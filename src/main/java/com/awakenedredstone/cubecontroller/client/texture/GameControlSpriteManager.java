package com.awakenedredstone.cubecontroller.client.texture;

import com.awakenedredstone.cubecontroller.CubeController;
import com.awakenedredstone.cubecontroller.client.GameControlInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasHolder;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class GameControlSpriteManager extends SpriteAtlasHolder {
    public GameControlSpriteManager(TextureManager textureManager) {
        super(textureManager, new Identifier(CubeController.MOD_ID, "textures/atlas/game_controls.png"), "game_control");
    }

    @Override
    protected Stream<Identifier> getSprites() {
        ArrayList<Identifier> list = new ArrayList<>(CubeController.GAME_CONTROL.getIds());
        list.add(CubeController.identifier("jump_captain"));
        return list.stream();
    }

    public Sprite getSprite(GameControlInfo control) {
        return this.getSprite(control.identifier());
    }

    public Sprite getSprite(Identifier identifier) {
        return super.getSprite(identifier);
    }
}
