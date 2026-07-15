package com.pkfl.creepyhorse.entity;

import com.pkfl.creepyhorse.registry.ModSounds;
import com.pkfl.creepyhorse.scenario.CreepyHorseScenario;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class CreepyHorseEntity
extends PathfinderMob
implements GeoEntity {
    private static final EntityDataAccessor<Boolean> SADDLED = SynchedEntityData.defineId(CreepyHorseEntity.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(CreepyHorseEntity.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private final Set<UUID> announcedKills = new HashSet<UUID>();
    private int blockBreakCooldown;

    public CreepyHorseEntity(EntityType<? extends CreepyHorseEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPersistenceRequired();
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 1000.0).add(Attributes.MOVEMENT_SPEED, 0.42).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.FOLLOW_RANGE, 96.0);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SADDLED, false);
        this.entityData.define(ANIMATION, "idle");
    }

    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag tag) {
        this.setNoAi(true);
        return super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
    }

    public boolean isSaddled() {
        return (Boolean)this.entityData.get(SADDLED);
    }

    public void setSaddled(boolean saddled) {
        this.entityData.set(SADDLED, saddled);
    }

    public String getAnimationName() {
        return (String)this.entityData.get(ANIMATION);
    }

    public void setAnimationName(String animation) {
        this.entityData.set(ANIMATION, animation);
    }

    public void standStill(String animation) {
        this.setNoAi(true);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.setAnimationName(animation);
    }

    public void pursue(Player player, double speed, boolean attack, boolean destroyBlocks) {
        if (player == null || !player.isAlive()) {
            return;
        }
        if (!attack && this.distanceToSqr((Entity)player) <= 4.0) {
            this.standStill("idle");
            return;
        }
        this.setNoAi(false);
        this.setAnimationName(attack ? "run" : "walk");
        this.getNavigation().moveTo((Entity)player, speed);
        if (destroyBlocks) {
            this.breakNearbyBlocks();
        }
        if (attack && this.distanceToSqr((Entity)player) < 6.25) {
            this.attackPlayer(player);
        }
    }

    public void pursueSurvival(Player player) {
        if (player == null || !player.isAlive()) {
            return;
        }
        this.setNoAi(false);
        this.setAnimationName(this.distanceToSqr((Entity)player) > 100.0 ? "hide_walk" : "run");
        this.getNavigation().moveTo((Entity)player, this.isInWater() ? 2.1 : 1.8);
        this.breakNearbyBlocks();
        if (this.distanceToSqr((Entity)player) < 6.25) {
            this.attackPlayer(player);
        }
    }

    private void attackPlayer(Player player) {
        if (player.hurt(this.damageSources().mobAttack((LivingEntity)this), Float.MAX_VALUE) && this.announcedKills.add(player.getUUID())) {
            this.level().playSound(null, this.blockPosition(), (SoundEvent)ModSounds.KILL_PLAYER.get(), SoundSource.HOSTILE, 1.0f, 1.0f);
        }
    }

    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation navigation = new GroundPathNavigation((Mob)this, level);
        navigation.setCanFloat(true);
        return navigation;
    }

    public boolean canBreatheUnderwater() {
        return true;
    }

    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.isInWater()) {
            this.setAirSupply(this.getMaxAirSupply());
            Vec3 velocity = this.getDeltaMovement();
            if (velocity.y < 0.08) {
                this.setDeltaMovement(velocity.x, Math.min(0.08, velocity.y + 0.025), velocity.z);
            }
        }
        if (!this.level().isClientSide && !this.isNoAi() && this.getNavigation().isDone() && this.isLocomotionAnimation()) {
            this.standStill("idle");
        }
    }

    public void runToward(Vec3 destination, double speed) {
        this.setNoAi(false);
        this.setAnimationName("run");
        this.getNavigation().moveTo(destination.x, destination.y, destination.z, speed);
        this.breakNearbyBlocks();
    }

    private void breakNearbyBlocks() {
        if (this.level().isClientSide || !this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return;
        }
        if (this.blockBreakCooldown-- > 0) {
            return;
        }
        this.blockBreakCooldown = 4;
        ServerLevel level = (ServerLevel)this.level();
        BlockPos center = this.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed((BlockPos)center.offset(-1, 0, -1), (BlockPos)center.offset(1, 2, 1))) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || !(state.getDestroySpeed((BlockGetter)level, pos) >= 0.0f) || level.getBlockEntity(pos) != null) continue;
            level.destroyBlock(pos, true, (Entity)this);
        }
    }

    private boolean isLocomotionAnimation() {
        String animation = this.getAnimationName();
        return animation.equals("walk") || animation.equals("run") || animation.equals("hide_walk") || animation.equals("hide_run");
    }

    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ServerPlayer serverPlayer;
        ItemStack held = player.getItemInHand(hand);
        if (!this.level().isClientSide && player instanceof ServerPlayer) {
            serverPlayer = (ServerPlayer)player;
            if (held.is(Items.SADDLE) && !this.isSaddled()) {
                this.setSaddled(true);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                CreepyHorseScenario.saddleHorse((ServerLevel)this.level(), this, serverPlayer);
                return InteractionResult.CONSUME;
            }
        }
        if (!this.level().isClientSide && player instanceof ServerPlayer) {
            serverPlayer = (ServerPlayer)player;
            if (held.is(Items.BRUSH)) {
                CreepyHorseScenario.beginBrushing((ServerLevel)this.level(), this, serverPlayer);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.sidedSuccess((boolean)this.level().isClientSide);
    }

    public boolean hurt(DamageSource source, float amount) {
        Entity entity;
        if (!this.level().isClientSide && (entity = source.getEntity()) instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)entity;
            CreepyHorseScenario.hitHorse((ServerLevel)this.level(), this, player);
        }
        return false;
    }

    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "movement", 0, state -> {
            String animation = this.getAnimationName();
            boolean looping = animation.equals("idle") || animation.equals("walk") || animation.equals("run") || animation.equals("flies") || animation.equals("hide_walk") || animation.equals("hide_idle");
            state.getController().setAnimation(RawAnimation.begin().then(animation, looping ? Animation.LoopType.LOOP : Animation.LoopType.PLAY_ONCE));
            return PlayState.CONTINUE;
        })});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
