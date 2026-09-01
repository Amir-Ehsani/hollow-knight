package com.hollowKnight.view.charm;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.hollowKnight.model.charm.CharmId;

public class CharmIconLibrary implements Disposable {
    private static final String ROOT = "Hollow Knight sprites/Inventory & UI/Charms/";

    private final ObjectMap<CharmId, Texture> textures = new ObjectMap<>();
    private final ObjectMap<CharmId, TextureRegion> regions = new ObjectMap<>();

    public CharmIconLibrary() {
        load(CharmId.SOUL_CATCHER, "Soul Catcher - _0001_charm_more_soul.png");
        load(CharmId.DASHMASTER, "Dashmaster - _0011_charm_generic_03.png");
        load(CharmId.UNBREAKABLE_STRENGTH, "Unbreakable Strength_0002_charm_glass_attack_up_full.png");
        load(CharmId.QUICK_SLASH, "Quick Slash - _0003_charm_nail_slash_speed_up.png");
        load(CharmId.QUICK_FOCUS, "Quick Focus - _0005_charm_fast_focus.png");
        load(CharmId.HEAVY_BLOW, "Heavy Blow - _0008_charm_nail_damage_up.png");
        load(CharmId.SHARP_SHADOW, "Sharp Shadow - charm_shade_impact.png");
        load(CharmId.VOID_HEART, "Void Heart - charm_black.png");
    }

    private void load(CharmId id, String fileName) {
        Texture texture = new Texture(ROOT + fileName);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        textures.put(id, texture);
        regions.put(id, new TextureRegion(texture));
    }

    public TextureRegion get(CharmId id) {
        TextureRegion region = regions.get(id);
        return region == null ? regions.get(CharmId.SOUL_CATCHER) : region;
    }

    @Override
    public void dispose() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
        regions.clear();
    }
}
