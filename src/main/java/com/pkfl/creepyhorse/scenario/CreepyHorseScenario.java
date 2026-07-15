package com.pkfl.creepyhorse.scenario;

import com.pkfl.creepyhorse.entity.CreepyHorseEntity;
import com.pkfl.creepyhorse.network.CreepyHorseNetwork;
import com.pkfl.creepyhorse.registry.ModEntities;
import com.pkfl.creepyhorse.registry.ModSounds;
import com.pkfl.creepyhorse.scenario.CreepyHorseData;
import com.pkfl.creepyhorse.scenario.ScenarioTask;
import java.util.List;
import java.util.Set;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

public final class CreepyHorseScenario {
    public static final String INTRO_PIG_TAG = "creepyhorse_intro_pig";
    private static final int SECOND = 20;
    private static final int MINUTE = 1200;
    private static final Set<Item> MEATS = Set.of(Items.BEEF, Items.COOKED_BEEF, Items.PORKCHOP, Items.COOKED_PORKCHOP, Items.MUTTON, Items.COOKED_MUTTON, Items.CHICKEN, Items.COOKED_CHICKEN, Items.RABBIT, Items.COOKED_RABBIT, Items.ROTTEN_FLESH, Items.COD, Items.COOKED_COD, Items.SALMON, Items.COOKED_SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH);

    private CreepyHorseScenario() {
    }

    public static void spawnIntroPig(ServerLevel level, ServerPlayer player) {
        CreepyHorseData data = CreepyHorseData.get(level);
        if (data.pigSpawned) {
            return;
        }
        Vec3 position = CreepyHorseScenario.positionInFront(level, player, 10.0, false);
        Pig pig = (Pig)EntityType.PIG.create((Level)level);
        if (pig == null) {
            return;
        }
        pig.moveTo(position.x, position.y, position.z, player.getYRot(), 0.0f);
        pig.setNoAi(true);
        pig.equipSaddle(SoundSource.NEUTRAL);
        pig.getPersistentData().putBoolean(INTRO_PIG_TAG, true);
        pig.setPersistenceRequired();
        level.addFreshEntity((Entity)pig);
        data.pigSpawned = true;
        data.setDirty();
    }

    public static boolean isIntroPig(Pig pig) {
        return pig.getPersistentData().getBoolean(INTRO_PIG_TAG);
    }

    public static void syncLoopSound(ServerLevel level, ServerPlayer player) {
        CreepyHorseData data = CreepyHorseData.get(level);
        if (data.loopSoundId.isEmpty()) {
            return;
        }
        ResourceLocation soundId = ResourceLocation.tryParse((String)data.loopSoundId);
        if (soundId != null) {
            CreepyHorseNetwork.sendLoopSound(player, soundId, true, 1.0f);
        }
    }

    public static ItemStack rulesBook() {
        ItemStack book = new ItemStack((ItemLike)Items.WRITTEN_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        tag.putString("title", "RULES");
        tag.putString("author", "PKFL & LookOut3D");
        ListTag pages = new ListTag();
        pages.add(StringTag.valueOf((String)Component.Serializer.toJson((Component)Component.literal((String)"DO NOT ASK WHY IT IS HERE.\nDO NOT ENCLOSE IT.\nLET IT OBSERVE.\nWHEN THE TIME COMES YOU MUST FIND IT.\nALWAYS KEEP A SADDLE NEAR.\nCOMPLETE WITH HONESTY AND INTEGRITY.\n\nTHREE VISITS.\nTHREE SADDLES."))));
        pages.add(StringTag.valueOf((String)Component.Serializer.toJson((Component)Component.literal((String)"DO NOT CONTINUE TO USE THIS MOD.\nDO NOT CONTINUE TO USE THIS MOD.\nDO NOT CONTINUE TO USE THIS MOD.\nDO NOT CONTINUE TO USE THIS MOD.\nDO NOT CONTINUE TO USE THIS MOD.\nDO NOT CONTINUE TO USE THIS MOD."))));
        tag.put("pages", (Tag)pages);
        return book;
    }

    public static void introPigKilled(ServerLevel level) {
        CreepyHorseData data = CreepyHorseData.get(level);
        if (data.pigKilled) {
            return;
        }
        data.pigKilled = true;
        CreepyHorseScenario.startTask(level, data, ScenarioTask.INTRO_WAIT, 4800);
    }

    public static void tick(ServerLevel level) {
        CreepyHorseData data = CreepyHorseData.get(level);
        if (CreepyHorseScenario.removeDuplicateHorses(level, data)) {
            data.setDirty();
        }
        if (data.task == ScenarioTask.NONE) {
            return;
        }
        long now = level.getGameTime();
        if (data.stripSaddlesAt > 0L && now >= data.stripSaddlesAt) {
            CreepyHorseScenario.stripPlayerSaddles(level);
            data.stripSaddlesAt = 0L;
            data.setDirty();
        }
        switch (data.task) {
            case INTRO_WAIT: {
                if (now < data.endsAt) break;
                CreepyHorseScenario.startTask(level, data, ScenarioTask.INTRO_STALK, 100);
                break;
            }
            case INTRO_STALK: {
                if (now < data.endsAt && !CreepyHorseScenario.playersNearHorse(level, data, 10.0)) break;
                CreepyHorseScenario.despawnHorse(level, data);
                CreepyHorseScenario.startDelay(level, data, ScenarioTask.SADDLE_30, 6000);
                break;
            }
            case COOLDOWN: {
                if (now < data.endsAt) break;
                CreepyHorseScenario.startTask(level, data, data.queuedTask, CreepyHorseScenario.duration(data.queuedTask));
                break;
            }
            case SADDLE_30: 
            case SADDLE_10: 
            case SADDLE_5: {
                CreepyHorseScenario.tickSaddleTask(level, data, now);
                break;
            }
            case SADDLED_PAUSE: {
                if (now < data.endsAt) break;
                CreepyHorseScenario.finishSaddledPause(level, data);
                break;
            }
            case NECK_HOLD: {
                CreepyHorseScenario.tickNeckHold(level, data, now);
                break;
            }
            case STALK_TWO: {
                if (now < data.endsAt && !CreepyHorseScenario.playersNearHorse(level, data, 10.0)) break;
                CreepyHorseScenario.despawnHorse(level, data);
                CreepyHorseScenario.startDelay(level, data, ScenarioTask.FLIES, 6000);
                break;
            }
            case FLIES: {
                CreepyHorseScenario.tickFlies(level, data, now);
                break;
            }
            case FOLLOW: {
                CreepyHorseScenario.tickFollow(level, data, now);
                break;
            }
            case ROAR_PAUSE: {
                if (now < data.endsAt) break;
                CreepyHorseScenario.startTask(level, data, ScenarioTask.FEED, CreepyHorseScenario.duration(ScenarioTask.FEED));
                break;
            }
            case FEED: {
                CreepyHorseScenario.tickFeed(level, data, now);
                break;
            }
            case RUN_AWAY: {
                CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
                if (horse != null) {
                    horse.runToward(horse.position().add(0.0, 0.0, 48.0), 6.0);
                }
                if (now < data.endsAt) break;
                CreepyHorseScenario.despawnHorse(level, data);
                CreepyHorseScenario.startDelay(level, data, ScenarioTask.ENTER, 12000);
                break;
            }
            case ENTER: {
                if (now < data.endsAt) break;
                CreepyHorseScenario.startTask(level, data, ScenarioTask.ENTER_CHASE, CreepyHorseScenario.duration(ScenarioTask.ENTER_CHASE));
                break;
            }
            case PENALTY_CHEAT: {
                if (now < data.endsAt) break;
                CreepyHorseScenario.startTask(level, data, ScenarioTask.PENALTY_NOTICE, 60);
                break;
            }
            case PENALTY_NOTICE: {
                if (now < data.endsAt) break;
                CreepyHorseScenario.startDelay(level, data, data.queuedTask, 200);
                break;
            }
            case PENALTY_FINAL: {
                if (now < data.endsAt) break;
                CreepyHorseScenario.startTask(level, data, ScenarioTask.PENALTY_SHORT, 60);
                break;
            }
            case PENALTY_SHORT: {
                if (now < data.endsAt) break;
                CreepyHorseScenario.startTask(level, data, ScenarioTask.HIDE, CreepyHorseScenario.duration(ScenarioTask.HIDE));
                break;
            }
            case HIDE: {
                CreepyHorseScenario.tickHide(level, data, now);
                break;
            }
            case SURVIVE: {
                CreepyHorseScenario.tickSurvive(level, data, now);
                break;
            }
            case ENTER_CHASE: {
                CreepyHorseScenario.tickEnterChase(level, data, now);
                break;
            }
            case CHASE: {
                CreepyHorseScenario.tickChase(level, data, now);
                break;
            }
        }
        if (now % 20L == 0L) {
            CreepyHorseScenario.sendOverlay(level, data, now);
        }
        data.setDirty();
    }

    public static void saddleHorse(ServerLevel level, CreepyHorseEntity horse, ServerPlayer player) {
        CreepyHorseData data = CreepyHorseData.get(level);
        if (!CreepyHorseScenario.isSaddleTask(data.task)) {
            horse.setSaddled(false);
            return;
        }
        ++data.saddleSuccesses;
        data.targetId = player.getUUID();
        horse.standStill("idle");
        CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.SUCCESS.get(), 1.0f);
        CreepyHorseScenario.startTask(level, data, ScenarioTask.SADDLED_PAUSE, data.saddleSuccesses >= 3 ? 3600 : 1200);
    }

    public static void beginBrushing(ServerLevel level, CreepyHorseEntity horse, ServerPlayer player) {
        CreepyHorseData data = CreepyHorseData.get(level);
        if (data.task != ScenarioTask.FLIES || horse != CreepyHorseScenario.horse(level, data)) {
            return;
        }
        data.brushPlayerId = player.getUUID();
        data.brushTicks = 0;
        data.setDirty();
    }

    public static void hitHorse(ServerLevel level, CreepyHorseEntity horse, ServerPlayer player) {
        CreepyHorseData data = CreepyHorseData.get(level);
        if (data.task != ScenarioTask.FLIES || horse != CreepyHorseScenario.horse(level, data)) {
            return;
        }
        ++data.flyHits;
        if (data.flyHits == 5) {
            CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.BRUSH_REQUIRED.get(), 1.0f);
        }
        data.setDirty();
    }

    public static void recordCheat(ServerLevel level) {
        CreepyHorseData data = CreepyHorseData.get(level);
        if (!data.cheatsDetected) {
            data.cheatsDetected = true;
            data.stripSaddlesAt = level.getGameTime() + 200L;
            data.setDirty();
        }
    }

    public static boolean commandsRevoked(ServerLevel level) {
        return CreepyHorseData.get((ServerLevel)level).commandsRevoked;
    }

    public static void setStage(ServerLevel level, int stage) {
        CreepyHorseData data = CreepyHorseData.get(level);
        CreepyHorseScenario.despawnHorse(level, data);
        data.stage = Mth.clamp((int)stage, (int)1, (int)4);
        if (data.stage == 4) {
            data.commandsRevoked = true;
            CreepyHorseScenario.startTask(level, data, ScenarioTask.FEED, 2400);
        } else if (data.stage == 3) {
            CreepyHorseScenario.startTask(level, data, ScenarioTask.HIDE, CreepyHorseScenario.duration(ScenarioTask.HIDE));
        } else if (data.stage == 2) {
            CreepyHorseScenario.startTask(level, data, ScenarioTask.FLIES, CreepyHorseScenario.duration(ScenarioTask.FLIES));
        } else {
            CreepyHorseScenario.startTask(level, data, ScenarioTask.SADDLE_30, CreepyHorseScenario.duration(ScenarioTask.SADDLE_30));
        }
    }

    public static void forceTask(ServerLevel level, ScenarioTask task) {
        CreepyHorseData data = CreepyHorseData.get(level);
        CreepyHorseScenario.despawnHorse(level, data);
        if (task == ScenarioTask.NONE) {
            CreepyHorseScenario.stopLoop(level, data);
            data.task = ScenarioTask.NONE;
            data.queuedTask = ScenarioTask.NONE;
            data.setDirty();
            return;
        }
        CreepyHorseScenario.startTask(level, data, task, CreepyHorseScenario.duration(task));
    }

    public static String status(ServerLevel level) {
        CreepyHorseData data = CreepyHorseData.get(level);
        long remaining = Math.max(0L, data.endsAt - level.getGameTime());
        return "Stage " + data.stage + " | " + String.valueOf((Object)data.task) + " | " + remaining / 20L + "s | cheats=" + data.cheatsDetected;
    }

    public static void reset(ServerLevel level) {
        CreepyHorseData data = CreepyHorseData.get(level);
        CreepyHorseScenario.stopLoop(level, data);
        CreepyHorseScenario.despawnHorse(level, data);
        level.getDataStorage().set("creepyhorse_scenario", (SavedData)new CreepyHorseData());
    }

    private static void tickSaddleTask(ServerLevel level, CreepyHorseData data, long now) {
        long remaining = Math.max(0L, data.endsAt - now);
        CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
        if (horse == null) {
            horse = CreepyHorseScenario.spawnHorse(level, data, CreepyHorseScenario.randomPlayer(level), 50.0, false, "idle", true);
        }
        if (data.task == ScenarioTask.SADDLE_30 && !data.cueShown && remaining <= 27600L) {
            data.cueShown = true;
            CreepyHorseScenario.despawnHorse(level, data);
            horse = CreepyHorseScenario.spawnHorse(level, data, CreepyHorseScenario.randomPlayer(level), 3.0, false, "idle", false);
            if (horse != null) {
                CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.HORSE_APPEAR.get(), 1.0f);
                data.cueAt = now + 100L;
            }
        }
        if (data.cueAt > 0L && now >= data.cueAt) {
            CreepyHorseEntity closeHorse = CreepyHorseScenario.horse(level, data);
            if (closeHorse != null) {
                closeHorse.standStill("waitingforsaddle");
            }
            data.cueAt = 0L;
        }
        float musicPitch = CreepyHorseScenario.saddleMusicPitch(data.task, remaining);
        if (data.nextSoundAt > 0L && now >= data.nextSoundAt) {
            CreepyHorseScenario.startLoop(level, data, (SoundEvent)ModSounds.HORSE_START.get(), musicPitch);
            data.nextSoundAt = 0L;
        } else if (CreepyHorseScenario.isLooping(data, (SoundEvent)ModSounds.HORSE_START.get()) && now % 20L == 0L) {
            CreepyHorseScenario.startLoop(level, data, (SoundEvent)ModSounds.HORSE_START.get(), musicPitch);
        }
        if (now >= data.endsAt) {
            CreepyHorseScenario.startTask(level, data, ScenarioTask.CHASE, CreepyHorseScenario.duration(ScenarioTask.CHASE));
        }
    }

    private static void finishSaddledPause(ServerLevel level, CreepyHorseData data) {
        CreepyHorseScenario.despawnHorse(level, data);
        ScenarioTask next = switch (data.saddleSuccesses) {
            case 1 -> ScenarioTask.SADDLE_10;
            case 2 -> ScenarioTask.NECK_HOLD;
            default -> ScenarioTask.STALK_TWO;
        };
        if (data.cheatsDetected && !data.penaltyShown) {
            data.penaltyShown = true;
            data.queuedTask = next;
            CreepyHorseScenario.startTask(level, data, ScenarioTask.PENALTY_CHEAT, 60);
        } else {
            CreepyHorseScenario.startDelay(level, data, next, next == ScenarioTask.STALK_TWO ? 3600 : 12000);
        }
    }

    private static void tickNeckHold(ServerLevel level, CreepyHorseData data, long now) {
        CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
        if (horse != null && data.cueAt > 0L && now >= data.cueAt) {
            horse.standStill("idle");
            data.cueAt = 0L;
        }
        if (now >= data.endsAt) {
            CreepyHorseScenario.despawnHorse(level, data);
            CreepyHorseScenario.startDelay(level, data, ScenarioTask.SADDLE_5, 12000);
        }
    }

    private static void tickFlies(ServerLevel level, CreepyHorseData data, long now) {
        CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
        if (horse == null) {
            horse = CreepyHorseScenario.spawnHorse(level, data, CreepyHorseScenario.randomPlayer(level), 3.0, true, "flies", false);
        }
        if (horse != null) {
            horse.standStill("flies");
            if (now % 5L == 0L) {
                Vec3 behind = horse.position().add(horse.getLookAngle().scale(-2.0));
                level.sendParticles((ParticleOptions)ParticleTypes.SMOKE, behind.x, behind.y + 1.4, behind.z, 2, 0.2, 0.2, 0.2, 0.01);
            }
        }
        if (data.flyHits >= 5) {
            float urgency;
            long remaining = Math.max(0L, data.endsAt - now);
            float f = urgency = remaining <= 6000L ? 1.0f - (float)remaining / 6000.0f : 0.0f;
            if (!CreepyHorseScenario.isLooping(data, (SoundEvent)ModSounds.BRUSH_THE_FLIES.get()) || now % 20L == 0L) {
                CreepyHorseScenario.startLoop(level, data, (SoundEvent)ModSounds.BRUSH_THE_FLIES.get(), 1.0f + urgency * 0.5f);
            }
        }
        CreepyHorseScenario.tickBrushProgress(level, data, horse);
        if (now >= data.endsAt) {
            CreepyHorseScenario.startTask(level, data, ScenarioTask.CHASE, CreepyHorseScenario.duration(ScenarioTask.CHASE));
        }
    }

    private static void tickBrushProgress(ServerLevel level, CreepyHorseData data, CreepyHorseEntity horse) {
        if (horse == null || data.brushPlayerId == null) {
            return;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(data.brushPlayerId);
        if (player == null || !player.isAlive() || player.distanceToSqr((Entity)horse) > 9.0 || !player.getMainHandItem().is(Items.BRUSH)) {
            data.brushTicks = 0;
            return;
        }
        ++data.brushTicks;
        if (data.brushTicks >= 100) {
            data.targetId = player.getUUID();
            CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.SUCCESS.get(), 1.0f);
            horse.standStill("idle");
            CreepyHorseScenario.startTask(level, data, ScenarioTask.FOLLOW, CreepyHorseScenario.duration(ScenarioTask.FOLLOW));
        }
    }

    private static void tickFollow(ServerLevel level, CreepyHorseData data, long now) {
        CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
        ServerPlayer target = CreepyHorseScenario.targetPlayer(level, data);
        if (horse != null && target != null) {
            horse.pursue((Player)target, 1.0, false, true);
        }
        if (now >= data.endsAt) {
            if (horse != null) {
                horse.standStill("roar");
            }
            CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.HORSE_ONE.get(), 1.0f);
            CreepyHorseScenario.startTask(level, data, ScenarioTask.ROAR_PAUSE, 200);
        }
    }

    private static void tickFeed(ServerLevel level, CreepyHorseData data, long now) {
        CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
        if (horse == null) {
            horse = CreepyHorseScenario.spawnHorse(level, data, CreepyHorseScenario.targetPlayer(level, data), 3.0, true, "idle", false);
        }
        if (horse == null) {
            return;
        }
        CreepyHorseScenario.consumeNearbyMeat(level, data, horse, now);
        long remaining = Math.max(0L, data.endsAt - now);
        if (data.cueAt > 0L && now >= data.cueAt) {
            horse.standStill("idle");
            data.cueAt = 0L;
        }
        if (remaining > 3600L) {
            if (CreepyHorseScenario.isLooping(data, (SoundEvent)ModSounds.FEED_THE_HORSE_TWO.get())) {
                CreepyHorseScenario.stopLoop(level, data);
            }
            if (now >= data.nextSoundAt) {
                CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.FEED_THE_HORSE.get(), 1.0f);
                data.nextSoundAt = now + 100L;
            }
        } else {
            float progress = 1.0f - (float)remaining / 3600.0f;
            if (!CreepyHorseScenario.isLooping(data, (SoundEvent)ModSounds.FEED_THE_HORSE_TWO.get()) || now % 20L == 0L) {
                CreepyHorseScenario.startLoop(level, data, (SoundEvent)ModSounds.FEED_THE_HORSE_TWO.get(), 1.0f + progress * 0.5f);
            }
        }
        if (now >= data.endsAt) {
            CreepyHorseScenario.startTask(level, data, ScenarioTask.CHASE, CreepyHorseScenario.duration(ScenarioTask.CHASE));
        }
    }

    private static void consumeNearbyMeat(ServerLevel level, CreepyHorseData data, CreepyHorseEntity horse, long now) {
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, horse.getBoundingBox().inflate(2.5))) {
            ItemStack stack = itemEntity.getItem();
            if (!MEATS.contains(stack.getItem())) continue;
            stack.shrink(1);
            if (stack.isEmpty()) {
                itemEntity.discard();
            }
            ++data.feedCount;
            horse.standStill("eat");
            data.cueAt = now + 20L;
            CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.NECK_MOVEMENT.get(), 1.0f);
            level.sendParticles((ParticleOptions)ParticleTypes.ITEM_SLIME, horse.getX(), horse.getY() + 1.2, horse.getZ(), 8, 0.4, 0.25, 0.4, 0.03);
            if (data.feedCount >= 10) {
                horse.standStill("neck_movement");
                data.cueAt = now + 40L;
                CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.SUCCESS.get(), 1.0f);
                CreepyHorseScenario.startTask(level, data, ScenarioTask.RUN_AWAY, 60);
            }
            return;
        }
    }

    private static void tickHide(ServerLevel level, CreepyHorseData data, long now) {
        ServerPlayer detected;
        CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
        if (horse == null && (horse = CreepyHorseScenario.spawnHorse(level, data, CreepyHorseScenario.randomPlayer(level), 28.0, true, "hide_idle", true)) != null) {
            horse.setGlowingTag(true);
        }
        long remaining = Math.max(0L, data.endsAt - now);
        if (now >= data.nextSoundAt) {
            float progress = remaining > 200L ? 0.0f : 1.0f - (float)remaining / 200.0f;
            CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.HIDE.get(), 1.0f + progress * 0.7f);
            data.nextSoundAt = now + Math.max(20L, (long)(100.0f * (1.0f - progress)));
        }
        if (horse != null && (detected = CreepyHorseScenario.nearestVisiblePlayer(level, horse, 18.0)) != null) {
            CreepyHorseScenario.startTask(level, data, ScenarioTask.SURVIVE, CreepyHorseScenario.duration(ScenarioTask.SURVIVE));
            return;
        }
        if (now >= data.endsAt) {
            CreepyHorseScenario.startTask(level, data, ScenarioTask.SURVIVE, CreepyHorseScenario.duration(ScenarioTask.SURVIVE));
        }
    }

    private static void tickSurvive(ServerLevel level, CreepyHorseData data, long now) {
        if (!CreepyHorseScenario.hasLivingPlayers(level)) {
            CreepyHorseScenario.stopSurviveAfterKills(level, data);
            return;
        }
        CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
        if (horse == null) {
            horse = CreepyHorseScenario.spawnHorse(level, data, CreepyHorseScenario.randomPlayer(level), 25.0, true, "hide_walk", true);
        }
        ServerPlayer target = CreepyHorseScenario.nearestPlayer(level, horse);
        if (horse != null && target != null) {
            horse.pursueSurvival((Player)target);
        }
        if (now >= data.endsAt) {
            CreepyHorseScenario.startTask(level, data, ScenarioTask.CHASE, CreepyHorseScenario.duration(ScenarioTask.CHASE));
        }
    }

    private static void tickChase(ServerLevel level, CreepyHorseData data, long now) {
        CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
        if (horse == null) {
            horse = CreepyHorseScenario.spawnHorse(level, data, CreepyHorseScenario.randomPlayer(level), 22.0, true, "run", false);
        }
        ServerPlayer target = CreepyHorseScenario.nearestPlayer(level, horse);
        if (horse != null && target != null) {
            horse.pursue((Player)target, 2.3, true, true);
        }
        if (!CreepyHorseScenario.isLooping(data, (SoundEvent)ModSounds.HORSE_CHASE.get())) {
            CreepyHorseScenario.startLoop(level, data, (SoundEvent)ModSounds.HORSE_CHASE.get(), 1.0f);
        }
        if (now >= data.endsAt) {
            CreepyHorseScenario.despawnHorse(level, data);
            if (data.stage >= 4) {
                ScenarioTask[] pool = new ScenarioTask[]{ScenarioTask.FLIES, ScenarioTask.FEED, ScenarioTask.ENTER};
                CreepyHorseScenario.startDelay(level, data, pool[level.random.nextInt(pool.length)], 12000);
            } else {
                data.stage = 4;
                data.commandsRevoked = true;
                CreepyHorseScenario.startTask(level, data, ScenarioTask.PENALTY_NOTICE, 60);
            }
        }
    }

    private static void tickEnterChase(ServerLevel level, CreepyHorseData data, long now) {
        CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
        if (horse == null) {
            horse = CreepyHorseScenario.spawnHorse(level, data, CreepyHorseScenario.randomPlayer(level), 25.0, true, "run", false);
        }
        ServerPlayer target = CreepyHorseScenario.nearestPlayer(level, horse);
        if (horse != null && target != null) {
            horse.pursue((Player)target, 2.3, true, true);
        }
        if (!CreepyHorseScenario.isLooping(data, (SoundEvent)ModSounds.HORSE_CHASE.get())) {
            CreepyHorseScenario.startLoop(level, data, (SoundEvent)ModSounds.HORSE_CHASE.get(), 1.0f);
        }
        if (now >= data.endsAt) {
            CreepyHorseScenario.despawnHorse(level, data);
            CreepyHorseScenario.startDelay(level, data, ScenarioTask.HIDE, 3600);
        }
    }

    private static void stopSurviveAfterKills(ServerLevel level, CreepyHorseData data) {
        CreepyHorseScenario.despawnHorse(level, data);
        data.task = ScenarioTask.NONE;
        data.queuedTask = ScenarioTask.NONE;
        data.endsAt = 0L;
        data.nextSoundAt = 0L;
        data.cueAt = 0L;
        CreepyHorseScenario.clearOverlay(level);
    }

    private static void startTask(ServerLevel level, CreepyHorseData data, ScenarioTask task, int duration) {
        long now = level.getGameTime();
        CreepyHorseScenario.stopLoop(level, data);
        data.task = task;
        data.endsAt = now + (long)duration;
        data.nextSoundAt = 0L;
        data.cueAt = 0L;
        data.cueShown = false;
        data.brushPlayerId = null;
        data.brushTicks = 0;
        switch (task) {
            case INTRO_STALK: {
                CreepyHorseEntity horse = CreepyHorseScenario.spawnHorse(level, data, CreepyHorseScenario.randomPlayer(level), 25.0, false, "idle", false);
                if (horse == null) break;
                CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.STALK.get(), 1.0f);
                break;
            }
            case SADDLE_30: 
            case SADDLE_10: 
            case SADDLE_5: {
                CreepyHorseScenario.despawnHorse(level, data);
                CreepyHorseEntity horse = CreepyHorseScenario.spawnHorse(level, data, CreepyHorseScenario.randomPlayer(level), 50.0, false, "idle", true);
                if (horse == null) break;
                CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.HORSE_SCREAM.get(), 1.0f);
                data.nextSoundAt = now + 100L;
                break;
            }
            case SADDLED_PAUSE: {
                CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
                if (horse == null) break;
                horse.standStill("idle");
                break;
            }
            case NECK_HOLD: {
                CreepyHorseEntity horse = CreepyHorseScenario.spawnHorse(level, data, CreepyHorseScenario.randomPlayer(level), 3.0, true, "neck_movement", false);
                if (horse == null) break;
                CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.NECK_MOVEMENT.get(), 1.0f);
                data.cueAt = now + 40L;
                break;
            }
            case STALK_TWO: {
                CreepyHorseEntity horse = CreepyHorseScenario.spawnHorse(level, data, CreepyHorseScenario.randomPlayer(level), 25.0, true, "idle", false);
                if (horse == null) break;
                CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.STALK.get(), 1.0f);
                break;
            }
            case FLIES: {
                data.flyHits = 0;
                CreepyHorseEntity horse = CreepyHorseScenario.spawnHorse(level, data, CreepyHorseScenario.randomPlayer(level), 3.0, true, "flies", false);
                if (horse == null) break;
                CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.FLIES_TASK.get(), 1.0f);
                break;
            }
            case FOLLOW: {
                CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
                if (horse == null) break;
                horse.setAnimationName("walk");
                break;
            }
            case ROAR_PAUSE: {
                CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
                if (horse == null) break;
                horse.standStill("roar");
                break;
            }
            case FEED: {
                data.feedCount = 0;
                CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
                if (horse == null) {
                    horse = CreepyHorseScenario.spawnHorse(level, data, CreepyHorseScenario.randomPlayer(level), 3.0, true, "idle", false);
                }
                if (horse != null) {
                    horse.setSaddled(true);
                    horse.standStill("idle");
                }
                CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.SECOND_TASK.get(), 1.0f);
                data.nextSoundAt = now;
                break;
            }
            case ENTER: {
                CreepyHorseScenario.despawnHorse(level, data);
                CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.ENTER_IN.get(), 1.0f);
                break;
            }
            case PENALTY_CHEAT: {
                CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.PENALTY.get(), 1.0f);
                break;
            }
            case PENALTY_NOTICE: {
                CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.PENALTY.get(), 1.0f);
                break;
            }
            case PENALTY_FINAL: {
                CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.PENALTY.get(), 1.0f);
                break;
            }
            case PENALTY_SHORT: {
                CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.PENALTY.get(), 1.0f);
                break;
            }
            case HIDE: {
                CreepyHorseScenario.despawnHorse(level, data);
                data.nextSoundAt = now;
                break;
            }
            case SURVIVE: {
                CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
                if (horse != null) {
                    horse.setGlowingTag(true);
                    horse.setAnimationName("hide_walk");
                }
                CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.TIME_IS_UP.get(), 1.0f);
                CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.HIDE_START.get(), 1.0f);
                CreepyHorseScenario.playAll(level, (SoundEvent)ModSounds.HIDE_TWO.get(), 1.0f);
                break;
            }
            case CHASE: {
                CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
                if (horse == null) {
                    CreepyHorseScenario.spawnHorse(level, data, CreepyHorseScenario.randomPlayer(level), 22.0, true, "run", false);
                }
                CreepyHorseScenario.startLoop(level, data, (SoundEvent)ModSounds.HORSE_CHASE.get(), 1.0f);
                break;
            }
            case ENTER_CHASE: {
                CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
                if (horse == null) {
                    CreepyHorseScenario.spawnHorse(level, data, CreepyHorseScenario.randomPlayer(level), 25.0, true, "run", false);
                }
                CreepyHorseScenario.startLoop(level, data, (SoundEvent)ModSounds.HORSE_CHASE.get(), 1.0f);
                break;
            }
        }
        data.setDirty();
    }

    private static void startDelay(ServerLevel level, CreepyHorseData data, ScenarioTask next, int delay) {
        CreepyHorseScenario.stopLoop(level, data);
        data.task = ScenarioTask.COOLDOWN;
        data.queuedTask = next;
        data.endsAt = level.getGameTime() + (long)delay;
        data.nextSoundAt = 0L;
        data.cueAt = 0L;
        data.setDirty();
    }

    private static int duration(ScenarioTask task) {
        return switch (task) {
            case INTRO_WAIT -> 4800;
            case INTRO_STALK -> 100;
            case SADDLE_30 -> 36000;
            case SADDLE_10, FLIES -> 12000;
            case SADDLE_5, FEED, HIDE -> 6000;
            case SADDLED_PAUSE -> 1200;
            case NECK_HOLD -> 4800;
            case STALK_TWO -> 80;
            case FOLLOW -> 6000;
            case ROAR_PAUSE -> 200;
            case RUN_AWAY -> 60;
            case ENTER, SURVIVE -> 3600;
            case PENALTY_CHEAT, PENALTY_NOTICE, PENALTY_FINAL, PENALTY_SHORT -> 60;
            case ENTER_CHASE -> 600;
            case CHASE -> 1200;
            default -> 0;
        };
    }

    private static boolean isSaddleTask(ScenarioTask task) {
        return task == ScenarioTask.SADDLE_30 || task == ScenarioTask.SADDLE_10 || task == ScenarioTask.SADDLE_5;
    }

    private static CreepyHorseEntity spawnHorse(ServerLevel level, CreepyHorseData data, ServerPlayer target, double distance, boolean saddled, String animation, boolean randomDirection) {
        if (target == null) {
            return null;
        }
        CreepyHorseScenario.despawnHorse(level, data);
        Vec3 position = CreepyHorseScenario.positionInFront(level, target, distance, randomDirection);
        CreepyHorseEntity horse = (CreepyHorseEntity)((EntityType)ModEntities.CREEPY_HORSE.get()).create((Level)level);
        if (horse == null) {
            return null;
        }
        horse.moveTo(position.x, position.y, position.z, 0.0f, 0.0f);
        CreepyHorseScenario.face(horse, target);
        horse.setSaddled(saddled);
        horse.standStill(animation);
        horse.setPersistenceRequired();
        horse.getPersistentData().putBoolean("creepyhorse_active", true);
        level.addFreshEntity((Entity)horse);
        data.horseId = horse.getUUID();
        data.targetId = target.getUUID();
        return horse;
    }

    private static CreepyHorseEntity horse(ServerLevel level, CreepyHorseData data) {
        CreepyHorseEntity horse;
        if (data.horseId == null) {
            return null;
        }
        Entity entity = level.getEntity(data.horseId);
        return entity instanceof CreepyHorseEntity ? (horse = (CreepyHorseEntity)entity) : null;
    }

    private static boolean removeDuplicateHorses(ServerLevel level, CreepyHorseData data) {
        CreepyHorseEntity keeper = CreepyHorseScenario.horse(level, data);
        boolean changed = false;
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof CreepyHorseEntity)) continue;
            CreepyHorseEntity creepyHorse = (CreepyHorseEntity)entity;
            if (keeper == null) {
                keeper = creepyHorse;
                data.horseId = keeper.getUUID();
                changed = true;
                continue;
            }
            if (creepyHorse == keeper) continue;
            creepyHorse.discard();
            changed = true;
        }
        return changed;
    }

    private static void despawnHorse(ServerLevel level, CreepyHorseData data) {
        CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
        if (horse != null) {
            horse.discard();
        }
        data.horseId = null;
    }

    private static ServerPlayer randomPlayer(ServerLevel level) {
        List<ServerPlayer> players = level.players().stream().filter(player -> player.isAlive() && !player.isSpectator()).toList();
        return players.isEmpty() ? null : players.get(level.random.nextInt(players.size()));
    }

    private static ServerPlayer targetPlayer(ServerLevel level, CreepyHorseData data) {
        ServerPlayer player;
        ServerPlayer serverPlayer = player = data.targetId == null ? null : level.getServer().getPlayerList().getPlayer(data.targetId);
        if (player != null && player.isAlive() && !player.isSpectator()) {
            return player;
        }
        player = CreepyHorseScenario.randomPlayer(level);
        if (player != null) {
            data.targetId = player.getUUID();
        }
        return player;
    }

    private static ServerPlayer nearestPlayer(ServerLevel level, CreepyHorseEntity horse) {
        if (horse == null) {
            return CreepyHorseScenario.randomPlayer(level);
        }
        return level.players().stream().filter(player -> player.isAlive() && !player.isSpectator()).min((a, b) -> Double.compare(a.distanceToSqr((Entity)horse), b.distanceToSqr((Entity)horse))).orElse(null);
    }

    private static boolean hasLivingPlayers(ServerLevel level) {
        return level.players().stream().anyMatch(player -> player.isAlive() && !player.isSpectator());
    }

    private static ServerPlayer nearestVisiblePlayer(ServerLevel level, CreepyHorseEntity horse, double range) {
        ServerPlayer player = CreepyHorseScenario.nearestPlayer(level, horse);
        return player != null && player.distanceToSqr((Entity)horse) <= range * range && horse.hasLineOfSight((Entity)player) ? player : null;
    }

    private static boolean playersNearHorse(ServerLevel level, CreepyHorseData data, double range) {
        CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
        return horse != null && level.players().stream().anyMatch(player -> player.isAlive() && player.distanceToSqr((Entity)horse) <= range * range);
    }

    private static Vec3 positionInFront(ServerLevel level, ServerPlayer player, double distance, boolean randomDirection) {
        Vec3 look = player.getLookAngle();
        if (randomDirection || Math.abs(look.x) + Math.abs(look.z) < 0.001) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            look = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
        }
        Vec3 horizontal = new Vec3(look.x, 0.0, look.z).normalize().scale(distance);
        int x = Mth.floor((double)(player.getX() + horizontal.x));
        int z = Mth.floor((double)(player.getZ() + horizontal.z));
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new Vec3((double)x + 0.5, (double)y, (double)z + 0.5);
    }

    private static void face(CreepyHorseEntity horse, ServerPlayer player) {
        Vec3 difference = player.position().subtract(horse.position());
        float yaw = (float)(Mth.atan2((double)difference.z, (double)difference.x) * 57.29577951308232) - 90.0f;
        horse.setYRot(yaw);
        horse.setYBodyRot(yaw);
        horse.setYHeadRot(yaw);
    }

    private static float saddleMusicPitch(ScenarioTask task, long remaining) {
        if (task == ScenarioTask.SADDLE_5) {
            return 0.85f;
        }
        if (task == ScenarioTask.SADDLE_10 && remaining <= 7200L) {
            if (remaining > 6000L) {
                return 0.85f;
            }
            float danger = 1.0f - (float)remaining / 6000.0f;
            return 0.85f + danger * 0.8f;
        }
        return 1.0f;
    }

    private static boolean isLooping(CreepyHorseData data, SoundEvent sound) {
        return sound.getLocation().toString().equals(data.loopSoundId);
    }

    private static void startLoop(ServerLevel level, CreepyHorseData data, SoundEvent sound, float pitch) {
        ResourceLocation soundId = sound.getLocation();
        if (!soundId.toString().equals(data.loopSoundId)) {
            CreepyHorseScenario.stopLoop(level, data);
            data.loopSoundId = soundId.toString();
        }
        for (ServerPlayer player : level.players()) {
            CreepyHorseNetwork.sendLoopSound(player, soundId, true, pitch);
        }
    }

    private static void stopLoop(ServerLevel level, CreepyHorseData data) {
        if (data.loopSoundId.isEmpty()) {
            return;
        }
        ResourceLocation soundId = ResourceLocation.tryParse((String)data.loopSoundId);
        if (soundId != null) {
            for (ServerPlayer player : level.players()) {
                CreepyHorseNetwork.sendLoopSound(player, soundId, false, 1.0f);
            }
        }
        data.loopSoundId = "";
    }

    private static void playAll(ServerLevel level, SoundEvent sound, float pitch) {
        for (ServerPlayer player : level.players()) {
            player.playNotifySound(sound, SoundSource.HOSTILE, 1.0f, pitch);
        }
    }

    private static void sendOverlay(ServerLevel level, CreepyHorseData data, long now) {
        Overlay overlay = CreepyHorseScenario.overlay(data, now);
        if (overlay == null) {
            return;
        }
        CreepyHorseEntity horse = CreepyHorseScenario.horse(level, data);
        boolean proximityDanger = (data.task == ScenarioTask.HIDE || data.task == ScenarioTask.SURVIVE) && horse != null && level.players().stream().anyMatch(player -> player.distanceToSqr((Entity)horse) < 400.0);
        for (ServerPlayer player2 : level.players()) {
            CreepyHorseNetwork.sendOverlay(player2, overlay.top, overlay.bottom, 40, overlay.bottomRed, overlay.danger || proximityDanger, overlay.glitch);
        }
    }

    private static void clearOverlay(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            CreepyHorseNetwork.sendOverlay(player, "", "", 0, false, false, false);
        }
    }

    private static Overlay overlay(CreepyHorseData data, long now) {
        long remaining = Math.max(0L, data.endsAt - now);
        String minutes = Math.max(0L, (long)Math.ceil((double)remaining / 1200.0)) + " MINUTES";
        boolean critical = remaining <= 200L;
        return switch (data.task) {
            case SADDLE_30, SADDLE_10, SADDLE_5 -> new Overlay(minutes, "SADDLE IT.", critical, critical, false);
            case FLIES -> new Overlay(minutes, data.flyHits >= 5 ? "BRUSH REQUIRED" : "SWAT THE FLIES.", data.flyHits >= 5 || remaining <= 6000L, critical, false);
            case FEED -> new Overlay(minutes, "FEED THE HORSE.", true, critical, false);
            case ENTER -> new Overlay(minutes, "ENTER THE ...", critical, critical, true);
            case HIDE -> new Overlay(minutes, "TO HIDE", true, false, false);
            case SURVIVE -> new Overlay(minutes, "SURVIVE", true, true, false);
            case PENALTY_CHEAT -> new Overlay("PLAYER ENCLOSED HORSE.", "PLAYER ENABLED CHEATS", true, false, false);
            case PENALTY_NOTICE -> new Overlay(data.stage >= 4 ? "PENALTY: TIMERS REDUCED." : "PENALTY: NEW TASKS ADDED", data.stage >= 4 ? "PENALTY: COMMANDS REVOKED" : "", true, false, false);
            case PENALTY_FINAL -> new Overlay("PLAYER FAILED FINAL TASK", "", true, false, false);
            case PENALTY_SHORT -> new Overlay("PENALTY: SHORTER TIMERS.", "", true, false, false);
            default -> null;
        };
    }

    private static void stripPlayerSaddles(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.is(Items.SADDLE)) continue;
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private record Overlay(String top, String bottom, boolean bottomRed, boolean danger, boolean glitch) {
    }
}
