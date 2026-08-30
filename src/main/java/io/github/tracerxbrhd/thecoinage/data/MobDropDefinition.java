package io.github.tracerxbrhd.thecoinage.data;

import com.google.gson.JsonObject;
import io.github.tracerxbrhd.thecoinage.api.currency.Denomination;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;

public record MobDropDefinition(ResourceLocation id, ResourceLocation entity, ResourceLocation entityTag,
                                double chance, Denomination denomination, int min, int max) {
    public static MobDropDefinition parse(ResourceLocation id, JsonObject json) {
        int format = GsonHelper.getAsInt(json, "format_version", 1);
        if (format != 1) throw new IllegalArgumentException("unsupported format_version " + format);
        ResourceLocation entity = json.has("entity") ? ResourceLocation.parse(json.get("entity").getAsString()) : null;
        ResourceLocation tag = json.has("entity_tag") ? ResourceLocation.parse(json.get("entity_tag").getAsString()) : null;
        if ((entity == null) == (tag == null)) throw new IllegalArgumentException("define exactly one of entity or entity_tag");
        double chance = GsonHelper.getAsDouble(json, "chance");
        if (!Double.isFinite(chance) || chance < 0 || chance > 1) throw new IllegalArgumentException("chance must be in 0..1");
        Denomination denomination = Denomination.byName(GsonHelper.getAsString(json, "denomination"))
            .orElseThrow(() -> new IllegalArgumentException("unknown denomination"));
        int min = GsonHelper.getAsInt(json, "min", 1);
        int max = GsonHelper.getAsInt(json, "max", min);
        if (min < 0 || max < min || max > 1_000_000) throw new IllegalArgumentException("invalid min/max");
        return new MobDropDefinition(id, entity, tag, chance, denomination, min, max);
    }

    public boolean matches(LivingEntity target) {
        if (entity != null) return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).equals(entity);
        return target.getType().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, entityTag));
    }
}
