package com.toast.apocalypse.common.entity.living;

import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.core.config.ApocalypseCommonConfig;
import com.toast.apocalypse.common.core.register.ApocalypseEffects;
import com.toast.apocalypse.common.core.register.ApocalypseItems;
import com.toast.apocalypse.common.core.register.ApocalypseSounds;
import com.toast.apocalypse.common.entity.living.ai.MobHurtByTargetGoal;
import com.toast.apocalypse.common.entity.living.ai.MoonMobPlayerTargetGoal;
import com.toast.apocalypse.common.entity.projectile.MonsterFishHook;
import com.toast.apocalypse.common.inventory.container.GrumpInventoryContainer;
import com.toast.apocalypse.common.misc.PlayerKeyBindInfo;
import com.toast.apocalypse.common.network.NetworkHelper;
import com.toast.apocalypse.common.triggers.ApocalypseTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fluids.FluidType;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

/**
 * This is a full moon mob that is meant to be a high threat to players that are not in a safe area from them.
 * Grumps fly, have a pulling attack, and have a melee attack that can't be reduced below 2 damage and applies a
 * short gravity effect. The pull attack/hook attack can be blocked with a shield, but blocking the hook twice or more
 * will enrage the grump, granting it a powerful speed and attack knockback bonus.<br><br>
 * Unlike most full moon mobs, this one has no means of breaking through defenses and therefore relies on the
 * player being vulnerable to attack - whether by will or by other mobs breaking through to the player.
 *
 * Despite being a fearsome close range enemy, it can be befriended with some Fatherly Toast, making it a useful
 * companion.
 */
public class Grump extends AbstractFullMoonGhast implements ContainerListener {

    protected static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(Grump.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<Boolean> ENRAGED = SynchedEntityData.defineId(Grump.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> STAND_BY = SynchedEntityData.defineId(Grump.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<ItemStack> HEAD_ITEM = SynchedEntityData.defineId(Grump.class, EntityDataSerializers.ITEM_STACK);

    private static final AttributeModifier RAGE_SPEED = new AttributeModifier("ApocalypseGrumpRAGE_SPEED", 2.0D, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier RAGE_KNOCKBACK = new AttributeModifier("ApocalypseGrumpRAGE_KNOCKBACK", 3.0D, AttributeModifier.Operation.ADDITION);


    /**The current fishhook entity launched by the grump. */
    private MonsterFishHook fishHook;
    private final MoveHelperController moveHelperController;

    protected final SimpleContainer inventory = new SimpleContainer(1);

    private int hookBlockedCount = 0;


    public Grump(EntityType<? extends Ghast> entityType, Level level) {
        super(entityType, level);
        moveHelperController = new MoveHelperController(this);
        moveControl = moveHelperController;
        xpReward = 3;
        inventory.addListener(this);
        updateContainerEquipment();
    }

    public static AttributeSupplier.Builder createGrumpAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FLYING_SPEED, 1.0D)
                .add(ForgeMod.SWIM_SPEED.get(), 1.1D)
                .add(Attributes.FOLLOW_RANGE, 4096.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OWNER_UUID, Optional.empty());
        entityData.define(STAND_BY, false);
        entityData.define(ENRAGED, false);
        entityData.define(HEAD_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new Grump.MeleeAttackGoal(this));
        goalSelector.addGoal(1, new LaunchMonsterHookGoal(this));
        goalSelector.addGoal(2, new Grump.FollowOwnerGoal(this));
        goalSelector.addGoal(3, new Grump.LookAtOwnerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(4, new GrumpLookAroundGoal(this));
        goalSelector.addGoal(5, new Grump.RandomFlyGoal(this));
        targetSelector.addGoal(0, new Grump.OwnerAttackerTargetGoal(this));
        targetSelector.addGoal(1, new GrumpHurtByTargetGoal(this, Enemy.class));
        targetSelector.addGoal(2, new MoonMobPlayerTargetGoal<>(this, true));
        targetSelector.addGoal(3, new GrumpNearestAttackableTargetGoal<>(this, Player.class));
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return 0.60F;
    }

    @Override
    protected float getSoundVolume() {
        // Not nearly as loud as a ghast since it is much smaller.
        return 2.0F;
    }

    @Override
    public boolean isPushedByFluid(FluidType fluidType) {
        return false; // Not pushed by fluids
    }

    public static boolean checkGrumpSpawnRules(EntityType<? extends Grump> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(entityType, level, spawnType, pos, random);
    }

    /** Limit arrow damage to max 1 if the Grump is wearing a Bucket Helmet. */
    @Override
    public boolean hurt(DamageSource damageSource, float damage) {
        if (damageSource.getDirectEntity() instanceof AbstractArrow) {
            if (getItemBySlot(EquipmentSlot.HEAD).getItem() == ApocalypseItems.BUCKET_HELM.get()) {
                damage = Math.min(damage, 1.0F);
            }
        }
        return super.hurt(damageSource, damage);
    }

    /** Apply Heavy effect on players on melee attack. */
    @Override
    public boolean doHurtTarget(Entity entity) {
        if (super.doHurtTarget(entity)) {
            if (entity instanceof Player player) {
                int duration = level.getDifficulty() == Difficulty.HARD ? 100 : 60;
                player.addEffect(new MobEffectInstance(ApocalypseEffects.HEAVY.get(), duration));
            }
            return true;
        }
        else {
            return false;
        }
    }

    /** Do not despawn if Grump has an owner. */
    @Override
    public void checkDespawn() {
        if (hasOwner())
            return;

        super.checkDespawn();
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (isEnraged()) {
            if (level.isClientSide) {
                level.addParticle(random.nextBoolean() ? ParticleTypes.SMOKE : ParticleTypes.CLOUD, getRandomX(0.5D), getY() + getBbHeight(), getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.getItem() == ApocalypseItems.FATHERLY_TOAST.get() && !hasOwner()) {
            if (!level.isClientSide) {
                if (level.getRandom().nextInt(4) == 0) {
                    tame(player, itemStack);
                    return InteractionResult.SUCCESS;
                }
                else {
                    usePlayerItem(player, itemStack);
                    level.broadcastEntityEvent(this, (byte)6);
                    return InteractionResult.CONSUME;
                }
            }
            return InteractionResult.CONSUME;
        }
        else if (itemStack.getItem() == Items.COOKIE) {
            if (getHealth() < getMaxHealth()) {
                heal((getMaxHealth() + 1.0F) / 6);
                performEatEffects(1);
                usePlayerItem(player, itemStack);
                return InteractionResult.SUCCESS;
            }
        }
        else {
            if (getOwner() == player) {
                if (player.isShiftKeyDown()) {
                    setStandBy(!shouldStandBy());
                }
                else {
                    if (getPassengers().isEmpty()) {
                        if (getHeadItem().getItem() == ApocalypseItems.BUCKET_HELM.get()) {
                            Block.popResource(level, blockPosition(), getHeadItem());
                            inventory.setItem(0, ItemStack.EMPTY);
                            return InteractionResult.SUCCESS;
                        }
                        player.startRiding(this);
                        return InteractionResult.SUCCESS;
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    protected void usePlayerItem(Player player, ItemStack itemStack) {
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }
    }

    private void tame(Player player, ItemStack itemStack) {
        usePlayerItem(player, itemStack);
        setOwnerUUID(player.getUUID());
        setEnraged(false, false);
        ApocalypseTriggers.TAMED_GRUMP.trigger((ServerPlayer) player, this);
        setTarget(null);
        setPlayerTargetUUID(null);
        // Stop potential weird movement happening
        // if a move goal was suddenly interrupted
        moveHelperController.setAction(MoveControl.Operation.WAIT);
        level.broadcastEntityEvent(this, (byte)7);
    }

    /**
     * Opens the Grump inventory and container for the given player.<br>
     * <br>
     * Requested from client when the player is riding<br>
     * a Grump and presses the inventory key binding.
     */
    public void openContainerForPlayer(ServerPlayer player) {
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        player.nextContainerCounter();
        AbstractContainerMenu container = new GrumpInventoryContainer(player.containerCounter, player.getInventory(), inventory, this);
        NetworkHelper.openGrumpInventory(player, container.containerId, this);
        player.containerMenu = container;
        player.initMenu(container);

        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.containerMenu));
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    public ItemStack getHeadItem() {
        return entityData.get(HEAD_ITEM);
    }

    public void setHeadItem(@Nullable ItemStack itemStack) {
        entityData.set(HEAD_ITEM, itemStack == null ? ItemStack.EMPTY : itemStack);
        setItemSlot(EquipmentSlot.HEAD, itemStack == null ? ItemStack.EMPTY : itemStack);
    }

    protected void updateContainerEquipment() {
        if (!level.isClientSide) {
            setHeadItem(inventory.getItem(0));
            setDropChance(EquipmentSlot.HEAD, 0.0F);
        }
    }

    /**
     * @param type Determines which particles should be displayed when the grump is fed.<br>
     *             <br>
     *             0 = Smoke<br>
     *             1 = Happy (Green star things)<br>
     *             2 = Heart
     */
    protected void performEatEffects(int type) {
        ParticleOptions particleType;

        if (type == 0) {
            particleType = ParticleTypes.SMOKE;
        }
        else {
            particleType = ParticleTypes.HEART;
        }
        for(int i = 0; i < 7; ++i) {
            double x = random.nextGaussian() * 0.02D;
            double y = random.nextGaussian() * 0.02D;
            double z = random.nextGaussian() * 0.02D;
            level.addParticle(particleType, getRandomX(1.0D), getRandomY() + 0.5D, getRandomZ(1.0D), x, y, z);
        }
        Vec3 pos = position();
        level.playLocalSound(pos.x(), pos.y(), pos.z(), SoundEvents.HORSE_EAT, SoundSource.NEUTRAL, 0.8F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.4F, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleEntityEvent(byte eventId) {
        if (eventId == 7) {
            performEatEffects(1);
        }
        else if (eventId == 6) {
            performEatEffects(0);
        }
        else {
            super.handleEntityEvent(eventId);
        }
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() && isVehicle();
    }

    @Nullable
    public LivingEntity getControllingPassenger() {
        if (getHeadItem().getItem() == Items.SADDLE) {
            Entity entity = getFirstPassenger();

            if (entity instanceof LivingEntity) {
                return (LivingEntity)entity;
            }
        }
        return null;
    }

    @Override
    public void travel(Vec3 vec) {
        if (isAlive()) {
            if (isVehicle()) {
                if (getControllingPassenger() != null) {
                    LivingEntity rider = getControllingPassenger();

                    setDeltaMovement(getDeltaMovement().scale(1.05D));

                    setYRot(rider.getYRot());
                    yRotO = getYRot();
                    setXRot(rider.getXRot() * 0.5F);
                    setRot(getYRot(), getXRot());
                    yBodyRot = getYRot();
                    yHeadRot = yBodyRot;

                    float xSpeed = rider.xxa;
                    float ySpeed = rider.yya;
                    float zSpeed = rider.zza;

                    if (rider instanceof Player player) {
                        if (PlayerKeyBindInfo.getInfo(player.getUUID()).grumpDescent.get()) {
                            ySpeed = -1.2F;
                        }
                        else if (player.jumping) {
                            ySpeed = 1.2F;
                        }
                    }
                    super.travel(new Vec3(xSpeed, ySpeed, zSpeed));
                }
            }
            else {
                super.travel(vec);
            }
        }
    }

    public void hookBlocked() {
        if (++hookBlockedCount >= 2) {
            setEnraged(true, true);
        }
    }

    public boolean isEnraged() {
        return entityData.get(ENRAGED);
    }

    public void setEnraged(boolean enraged, boolean playEffects) {
        entityData.set(ENRAGED, enraged);

        if (enraged) {
            if (!getAttribute(Attributes.FLYING_SPEED).hasModifier(RAGE_SPEED))
                getAttribute(Attributes.FLYING_SPEED).addTransientModifier(RAGE_SPEED);
            if (!getAttribute(Attributes.ATTACK_KNOCKBACK).hasModifier(RAGE_KNOCKBACK))
                getAttribute(Attributes.ATTACK_KNOCKBACK).addTransientModifier(RAGE_KNOCKBACK);
        }
        else {
            getAttribute(Attributes.FLYING_SPEED).removeModifier(RAGE_SPEED);
            getAttribute(Attributes.ATTACK_KNOCKBACK).removeModifier(RAGE_KNOCKBACK);
        }

        if (playEffects) {
            playSound(ApocalypseSounds.GRUMP_RAGE.get());
        }
    }

    public void setOwnerUUID(UUID uuid) {
        entityData.set(OWNER_UUID, Optional.of(uuid));
    }

    @Nullable
    public UUID getOwnerUUID() {
        return entityData.get(OWNER_UUID).orElse(null);
    }

    public boolean hasOwner() {
        return getOwnerUUID() != null;
    }

    @Nullable
    public LivingEntity getOwner() {
        try {
            UUID uuid = getOwnerUUID();
            return uuid == null ? null : level.getPlayerByUUID(uuid);
        }
        catch (IllegalArgumentException exception) {
            return null;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean shouldStandBy() {
        return entityData.get(STAND_BY);
    }

    public void setStandBy(boolean standBy) {
        entityData.set(STAND_BY, standBy);
    }


    @SuppressWarnings("ConstantConditions")
    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);

        compoundTag.putBoolean("StandBy", entityData.get(STAND_BY));
        compoundTag.putBoolean("Enraged", entityData.get(ENRAGED));

        if (hasOwner()) {
            compoundTag.putUUID("Owner", getOwnerUUID());
        }
        compoundTag.put("HeadItem", entityData.get(HEAD_ITEM).save(new CompoundTag()));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);

        setStandBy(compoundTag.getBoolean("StandBy"));
        setEnraged(compoundTag.getBoolean("Enraged"), false);

        if (compoundTag.contains("HeadItem", compoundTag.getId())) {
            ItemStack headItem = ItemStack.of(compoundTag.getCompound("HeadItem"));
            inventory.setItem(0, headItem);
            setHeadItem(headItem);
        }
        UUID uuid;

        if (compoundTag.hasUUID("Owner")) {
            uuid = compoundTag.getUUID("Owner");
        }
        else {
            String s = compoundTag.getString("Owner");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(getServer(), s);
        }
        if (uuid != null) {
            try {
                setOwnerUUID(uuid);
            }
            catch (Throwable ignored) {

            }
        }
        updateContainerEquipment();
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficultyInstance, MobSpawnType spawnType, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag compoundTag) {
        spawnData = super.finalizeSpawn(level, difficultyInstance, spawnType, spawnData, compoundTag);
        populateDefaultEquipmentSlots(level.getRandom(), difficultyInstance);
        return spawnData;
    }


    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficultyInstance) {
        double chance = ApocalypseCommonConfig.COMMON.getGrumpBucketHelmetChance();

        if (chance <= 0)
            return;

        if (random.nextDouble() <= chance) {
            setHeadItem(new ItemStack(ApocalypseItems.BUCKET_HELM.get()));
        }
    }

    @Override
    public void containerChanged(Container container) {
        ItemStack itemStack = container.getItem(0);
        setHeadItem(itemStack);

        if (itemStack.getItem() == ApocalypseItems.BUCKET_HELM.get()) {
            getPassengers().forEach(Entity::stopRiding);
        }
        else if (tickCount > 20 && itemStack.getItem() == Items.SADDLE || itemStack.getItem() == ApocalypseItems.BUCKET_HELM.get()) {
            playSound(SoundEvents.HORSE_SADDLE, 0.5F, 1.0F);
        }
    }

    private static class MeleeAttackGoal extends Goal {

        final Grump grump;

        public MeleeAttackGoal(Grump grump) {
            setFlags(EnumSet.of(Flag.MOVE));
            this.grump = grump;
        }

        private void setWantedPosition(LivingEntity target) {
            Vec3 vec3 = target.getEyePosition(1.0F).add(0.0D, -(grump.getBbHeight() / 2), 0.0D);
            double speed = grump.getAttributeValue(Attributes.FLYING_SPEED);
            grump.moveControl.setWantedPosition(vec3.x, vec3.y, vec3.z, speed);
        }

        @Override
        public boolean canUse() {
            LivingEntity target = grump.getTarget();
            return target != null && grump.canSeeDirectly(target);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = grump.getTarget();

            if (!grump.isVehicle() && target != null && target.isAlive()) {
                return grump.moveControl.hasWanted() && grump.moveHelperController.canReachCurrentWanted();
            }
            return false;
        }

        @Override
        public void start() {
            LivingEntity target = grump.getTarget();

            if (target != null) {
                setWantedPosition(target);
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void stop() {
            grump.moveHelperController.setAction(MoveControl.Operation.WAIT);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void tick() {
            LivingEntity target = grump.getTarget();

            if (grump.getBoundingBox().inflate(0.3F).intersects(target.getBoundingBox())) {
                grump.doHurtTarget(target);
            }
            else {
                if ((grump.tickCount & 20) == 0) {
                    setWantedPosition(target);
                }
            }
        }
    }

    private static class LaunchMonsterHookGoal extends Goal {

        private final Grump grump;
        private int timeHookExisted;
        private int timeNextHookLaunch;

        public LaunchMonsterHookGoal(Grump grump) {
            this.grump = grump;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            if (!grump.getEyeInFluidType().isAir()) {
                return false;
            }

            if (!grump.isVehicle() && grump.getTarget() != null) {
                LivingEntity target = grump.getTarget();
                return grump.hasLineOfSight(target) && grump.distanceToSqr(target) < 180.0D;
            }
            return false;
        }

        @Override
        public void start() {
            // Wait a bit before launching the hook, as the grump's
            // body rotation usually don't get updated before
            // this AI task is started, resulting in the hook
            // being launched where the Grump was looking before
            // targeting a player.
            timeNextHookLaunch = 20;
        }

        @Override
        public void stop() {
            if (grump.fishHook != null) {
                grump.fishHook.discard();
                grump.fishHook = null;
            }
            timeHookExisted = 0;
            timeNextHookLaunch = 0;
        }

        @Override
        public void tick() {
            MonsterFishHook hook = grump.fishHook;

            if (hook == null) {
                if (++timeNextHookLaunch >= 40) {
                    spawnMonsterFishHook(grump.getTarget());
                    timeNextHookLaunch = 0;
                }
            }
            else {
                if (hook.getHookedIn() != null) {
                    // The grump might end up accidentally hooking itself, who knows?
                    if (hook.getHookedIn() != grump) {
                        hook.bringInHookedEntity();
                    }
                    removeMonsterFishHook();
                    return;
                }

                if (++timeHookExisted >= 60) {
                    timeHookExisted = 0;
                    removeMonsterFishHook();
                }
            }
        }

        private void removeMonsterFishHook() {
            grump.fishHook.discard();
            grump.fishHook = null;
        }

        private void spawnMonsterFishHook(@Nullable LivingEntity target) {
            if (target == null)
                return;

            Level level = grump.getCommandSenderWorld();
            MonsterFishHook fishHook = new MonsterFishHook(grump, target, level);
            level.addFreshEntity(fishHook);
            grump.fishHook = fishHook;
            level.playSound(null, grump.blockPosition(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.6F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
        }
    }

    private static class OwnerAttackerTargetGoal extends TargetGoal {

        private final Grump grump;
        private LivingEntity target;

        public OwnerAttackerTargetGoal(Grump grump) {
            super(grump, true, true);
            this.grump = grump;
        }

        @Override
        public boolean canUse() {
            if (grump.isVehicle())
                return false;

            if (grump.getOwner() != null && grump.getOwner().isAlive() && !grump.shouldStandBy()) {
                LivingEntity owner = grump.getOwner();

                LivingEntity target = owner.getLastHurtByMob() == null ? owner.getLastHurtMob() : owner.getLastHurtByMob();

                if (target != null && target != grump && target != owner && !(target instanceof Creeper)) {
                    // Is the target owned by our owner?
                    if (target instanceof TamableAnimal tamableAnimal && tamableAnimal.isOwnedBy(owner)) {
                        return false;
                    }
                    // Is the target a grump also owned by my owner?
                    else if (target instanceof Grump targetGrump && targetGrump.getOwner() == owner) {
                        return false;
                    }
                    this.target = target;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            mob.setTarget(target);
            super.start();
        }
    }

    private static class GrumpHurtByTargetGoal extends MobHurtByTargetGoal {

        private final Grump grump;

        public GrumpHurtByTargetGoal(Grump grump, Class<?>... toIgnoreDamage) {
            super(grump, toIgnoreDamage);
            setFlags(EnumSet.of(Flag.TARGET));
            this.grump = grump;
        }

        @Override
        public boolean canUse() {
            if (grump.getLastHurtByMob() == grump.getOwner())
                return false;

            return super.canUse();
        }

        @Override
        protected boolean canAttack(@Nullable LivingEntity target, TargetingConditions conditions) {
            boolean canAttack = super.canAttack(target, conditions);
            return canAttack && target != grump.getOwner();
        }
    }

    private static class GrumpNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {

        private final Grump grump;

        public GrumpNearestAttackableTargetGoal(Grump grump, Class<T> targetClass) {
            super(grump, targetClass, true);
            this.grump = grump;
        }

        /** Friggin' large bounding box */
        @Override
        protected AABB getTargetSearchArea(double followRange) {
            return mob.getBoundingBox().inflate(followRange, followRange, followRange);
        }

        @Override
        public void setTarget(@org.jetbrains.annotations.Nullable LivingEntity target) {
            Apocalypse.LOGGER.info("Target: " + target);
            super.setTarget(target);
        }

        @Override
        public boolean canUse() {
            return !grump.hasOwner() && super.canUse();
        }

        @Override
        protected boolean canAttack(@Nullable LivingEntity livingEntity, TargetingConditions conditions) {
            boolean canAttack = super.canAttack(target, conditions);

            if (canAttack) {
                if (grump.hasOwner())
                    return !target.getUUID().equals(grump.getOwnerUUID());
            }
            return canAttack;
        }
    }

    /** Copied from ghast */
    static class RandomFlyGoal extends Goal {

        private final Grump grump;

        public RandomFlyGoal(Grump grump) {
            this.grump = grump;
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (grump.hasOwner() || grump.isVehicle())
                return false;

            if (grump.getTarget() != null) {
                return false;
            }
            MoveControl moveControl = grump.getMoveControl();

            if (!moveControl.hasWanted()) {
                return true;
            }
            else {
                double x = moveControl.getWantedX() - grump.getX();
                double y = moveControl.getWantedY() - grump.getY();
                double z = moveControl.getWantedZ() - grump.getZ();
                double d3 = x * x + y * y + z * z;
                return d3 < 1.0D || d3 > 3600.0D;
            }
        }

        @Override
        public void stop() {

        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            RandomSource random = grump.getRandom();
            double x = grump.getX() + (double)((random.nextFloat() * 2.0F - 1.0F) * 8.0F);
            double y = grump.getY() + (double)((random.nextFloat() * 2.0F - 1.0F) * 8.0F);
            double z = grump.getZ() + (double)((random.nextFloat() * 2.0F - 1.0F) * 8.0F);
            double speed = grump.getAttributeValue(Attributes.FLYING_SPEED);
            grump.getMoveControl().setWantedPosition(x, y, z, speed);
        }
    }

    /** Mostly copy-pasted from {@link net.minecraft.world.entity.ai.goal.FollowOwnerGoal} */
    @SuppressWarnings("ConstantConditions")
    static class FollowOwnerGoal extends Goal {

        private final Grump grump;
        private LivingEntity owner;

        public FollowOwnerGoal(Grump grump) {
            this.grump = grump;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        private void setWantedToOwner() {
            Vec3 vec3 = owner.getEyePosition(1.0F).add(0.0D, -(grump.getBbHeight() / 2), 0.0D);
            grump.moveControl.setWantedPosition(vec3.x, vec3.y, vec3.z, 1.0D);
        }

        private void setWantedPosition(double x, double y, double z) {
            grump.moveControl.setWantedPosition(x, y, z, 1.0D);
        }

        @Override
        public boolean canUse() {
            return grump.getOwner() != null && !grump.shouldStandBy() && !grump.isVehicle() && grump.distanceToSqr(grump.getOwner()) > 80.0D;
        }

        @Override
        public boolean canContinueToUse() {
            if (owner != null && owner.isAlive() && !grump.shouldStandBy() && !grump.isVehicle() && grump.distanceToSqr(owner) > 40.0D) {
                return grump.moveControl.hasWanted() && grump.moveHelperController.canReachCurrentWanted();
            }
            return false;
        }

        @Override
        public void start() {
            owner = grump.getOwner();
        }

        @Override
        public void stop() {
            grump.moveHelperController.setAction(MoveControl.Operation.WAIT);
        }

        @Override
        public void tick() {
            if (grump.distanceToSqr(owner) > 200) {
                teleportToOwner();
            }
            setWantedToOwner();
        }

        private void teleportToOwner() {
            BlockPos blockpos = grump.getOwner().blockPosition();

            for(int i = 0; i < 10; ++i) {
                int x = randomIntInclusive(-3, 3);
                int y = randomIntInclusive(-1, 1);
                int z = randomIntInclusive(-3, 3);

                boolean teleported = maybeTeleportTo(blockpos.getX() + x, blockpos.getY() + y, blockpos.getZ() + z);

                if (teleported) {
                    return;
                }
            }
        }

        private boolean maybeTeleportTo(int x, int y, int z) {
            if (Math.abs((double)x - owner.getX()) < 2.0D && Math.abs((double)z - owner.getZ()) < 2.0D) {
                return false;
            }
            else if (!canTeleportTo(new BlockPos(x, y, z))) {
                return false;
            }
            else {
                grump.moveTo((double)x + 0.5D, y + 1.0D, (double)z + 0.5D, grump.getYRot(), grump.getXRot());
                setWantedPosition(grump.getX(), grump.getY(), grump.getZ());
                return true;
            }
        }

        private boolean canTeleportTo(BlockPos pos) {
            BlockPathTypes pathType = WalkNodeEvaluator.getBlockPathTypeStatic(grump.level, pos.mutable());

            if (pathType != BlockPathTypes.WALKABLE) {
                return false;
            }
            else {
                return grump.level.noCollision(grump, grump.getBoundingBox().move(pos.above()).inflate(2));
            }
        }

        private int randomIntInclusive(int minBound, int maxBound) {
            return grump.getRandom().nextInt(maxBound - minBound + 1) + minBound;
        }
    }

    protected static class GrumpLookAroundGoal extends Goal {
        private final Grump grump;

        public GrumpLookAroundGoal(Grump grump) {
            this.grump = grump;
            setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return true;
        }

        public void tick() {
            if (grump.getTarget() == null) {
                Vec3 vec3 = grump.getDeltaMovement();
                grump.setYRot(-((float) Mth.atan2(vec3.x, vec3.z)) * (180F / (float)Math.PI));
            }
            else {
                LivingEntity target = grump.getTarget();

                double x = target.getX() - grump.getX();
                double z = target.getZ() - grump.getZ();
                grump.setYRot(-((float) Mth.atan2(x, z)) * (180F / (float)Math.PI));
            }
            grump.yBodyRot = grump.getYRot();
        }
    }

    private static class LookAtOwnerGoal extends LookAtPlayerGoal {

        private final Grump grump;

        public LookAtOwnerGoal(Grump grump, Class<? extends LivingEntity> livingEntity, float lookDist) {
            super(grump, livingEntity, lookDist);
            setFlags(EnumSet.of(Flag.LOOK));
            this.grump = grump;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public boolean canUse() {
            if (!grump.hasOwner())
                return false;

            if (mob.getRandom().nextFloat() >= probability) {
                return false;
            }
            else {
                if (grump.getOwner() != null) {
                    lookAt = grump.getOwner();
                }
                return lookAt != null;
            }
        }
    }
}
