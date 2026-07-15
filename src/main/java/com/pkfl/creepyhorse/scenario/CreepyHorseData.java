package com.pkfl.creepyhorse.scenario;

import com.pkfl.creepyhorse.scenario.ScenarioTask;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class CreepyHorseData
extends SavedData {
    private static final String NAME = "creepyhorse_scenario";
    public ScenarioTask task = ScenarioTask.NONE;
    public ScenarioTask queuedTask = ScenarioTask.NONE;
    public long endsAt;
    public long nextSoundAt;
    public long cueAt;
    public String loopSoundId = "";
    public int stage = 1;
    public int saddleSuccesses;
    public int brushTicks;
    public int flyHits;
    public int feedCount;
    public boolean pigSpawned;
    public boolean pigKilled;
    public boolean cheatsDetected;
    public boolean penaltyShown;
    public boolean commandsRevoked;
    public boolean cueShown;
    public UUID horseId;
    public UUID targetId;
    public UUID brushPlayerId;
    public long stripSaddlesAt;

    public static CreepyHorseData get(ServerLevel level) {
        return (CreepyHorseData)level.getDataStorage().computeIfAbsent(CreepyHorseData::load, CreepyHorseData::new, NAME);
    }

    public static CreepyHorseData load(CompoundTag tag) {
        CreepyHorseData data = new CreepyHorseData();
        data.task = CreepyHorseData.task(tag.getString("Task"));
        data.queuedTask = CreepyHorseData.task(tag.getString("QueuedTask"));
        data.endsAt = tag.getLong("EndsAt");
        data.nextSoundAt = tag.getLong("NextSoundAt");
        data.cueAt = tag.getLong("CueAt");
        data.loopSoundId = tag.getString("LoopSoundId");
        data.stage = tag.getInt("Stage");
        data.saddleSuccesses = tag.getInt("SaddleSuccesses");
        data.brushTicks = tag.getInt("BrushTicks");
        data.flyHits = tag.getInt("FlyHits");
        data.feedCount = tag.getInt("FeedCount");
        data.pigSpawned = tag.getBoolean("PigSpawned");
        data.pigKilled = tag.getBoolean("PigKilled");
        data.cheatsDetected = tag.getBoolean("CheatsDetected");
        data.penaltyShown = tag.getBoolean("PenaltyShown");
        data.commandsRevoked = tag.getBoolean("CommandsRevoked");
        data.cueShown = tag.getBoolean("CueShown");
        data.horseId = CreepyHorseData.readUuid(tag, "HorseId");
        data.targetId = CreepyHorseData.readUuid(tag, "TargetId");
        data.brushPlayerId = CreepyHorseData.readUuid(tag, "BrushPlayerId");
        data.stripSaddlesAt = tag.getLong("StripSaddlesAt");
        return data;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putString("Task", this.task.name());
        tag.putString("QueuedTask", this.queuedTask.name());
        tag.putLong("EndsAt", this.endsAt);
        tag.putLong("NextSoundAt", this.nextSoundAt);
        tag.putLong("CueAt", this.cueAt);
        tag.putString("LoopSoundId", this.loopSoundId);
        tag.putInt("Stage", this.stage);
        tag.putInt("SaddleSuccesses", this.saddleSuccesses);
        tag.putInt("BrushTicks", this.brushTicks);
        tag.putInt("FlyHits", this.flyHits);
        tag.putInt("FeedCount", this.feedCount);
        tag.putBoolean("PigSpawned", this.pigSpawned);
        tag.putBoolean("PigKilled", this.pigKilled);
        tag.putBoolean("CheatsDetected", this.cheatsDetected);
        tag.putBoolean("PenaltyShown", this.penaltyShown);
        tag.putBoolean("CommandsRevoked", this.commandsRevoked);
        tag.putBoolean("CueShown", this.cueShown);
        CreepyHorseData.writeUuid(tag, "HorseId", this.horseId);
        CreepyHorseData.writeUuid(tag, "TargetId", this.targetId);
        CreepyHorseData.writeUuid(tag, "BrushPlayerId", this.brushPlayerId);
        tag.putLong("StripSaddlesAt", this.stripSaddlesAt);
        return tag;
    }

    private static ScenarioTask task(String value) {
        try {
            return ScenarioTask.valueOf(value);
        }
        catch (IllegalArgumentException ignored) {
            return ScenarioTask.NONE;
        }
    }

    private static UUID readUuid(CompoundTag tag, String key) {
        return tag.hasUUID(key) ? tag.getUUID(key) : null;
    }

    private static void writeUuid(CompoundTag tag, String key, UUID value) {
        if (value != null) {
            tag.putUUID(key, value);
        }
    }
}

