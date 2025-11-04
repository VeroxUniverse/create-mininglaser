package net.veroxuniverse.create_mininglaser.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.foundation.element.InputWindowElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

public class MiningLaserPonderScenes {

    private static final BlockPos CONTROLLER_POS = new BlockPos(2, 2, 2);
    private static final BlockPos HATCH_POS      = new BlockPos(3, 2, 2);

    private static ItemStack item(String id) {
        Item it = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        return it != null ? new ItemStack(it) : ItemStack.EMPTY;
    }

    private static void makeFloor(CreateSceneBuilder scene, SceneBuildingUtil util) {
        for (int x = 0; x < 5; x++)
            for (int z = 0; z < 5; z++)
                scene.world().setBlock(util.grid().at(x, 0, z), Blocks.SMOOTH_STONE.defaultBlockState(), false);
        scene.world().showSection(util.select().layer(0), Direction.UP);
    }

    public static void laserMultiblock(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("laser_multiblock", "Building the Laser Drill Multiblock");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        makeFloor(scene, util);

        Selection layer1     = util.select().layer(1);
        Selection layer2All  = util.select().layer(2);
        Selection controller = util.select().position(CONTROLLER_POS);
        Selection hatch      = util.select().position(HATCH_POS);
        Selection layer2Rest = layer2All.substract(controller).substract(hatch);

        scene.idle(10);
        scene.world().showSection(layer1, Direction.DOWN);
        scene.idle(20);
        scene.overlay().showText(60).attachKeyFrame()
                .text("Place the first layer of Drill Casings.")
                .placeNearTarget();
        scene.idle(65);

        scene.world().showSection(layer2Rest, Direction.DOWN);
        scene.idle(20);

        scene.world().showSection(controller, Direction.DOWN);
        scene.overlay().showOutline(PonderPalette.GREEN, controller, controller, 60);
        scene.overlay().showText(60).attachKeyFrame()
                .text("Place the Controller here.")
                .pointAt(controller.getCenter())
                .placeNearTarget();
        scene.idle(65);

        scene.world().showSection(hatch, Direction.DOWN);
        scene.overlay().showOutline(PonderPalette.BLUE, hatch, hatch, 60);
        scene.overlay().showText(60).attachKeyFrame()
                .text("Place the Hatch on the side for item output.")
                .pointAt(hatch.getCenter())
                .placeNearTarget();
        scene.idle(65);

        scene.overlay()
                .showControls(util.vector().topOf(CONTROLLER_POS).add(0, 0.25, 0), Pointing.RIGHT, 60)
                .rightClick()
                .withItem(item("create:wrench"));
        scene.overlay().showText(60).attachKeyFrame()
                .text("Use a Wrench on the Controller to form the multiblock.")
                .pointAt(controller.getCenter())
                .placeNearTarget();
        scene.idle(65);
    }

    public static void insertCore(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("insert_core", "Inserting and Removing Drill Cores");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        makeFloor(scene, util);

        Selection controller = util.select().position(CONTROLLER_POS);

        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showOutline(PonderPalette.GREEN, controller, controller, 60);

        scene.overlay()
                .showControls(util.vector().topOf(CONTROLLER_POS).add(0, 0.25, 0), Pointing.RIGHT, 60)
                .rightClick()
                .withItem(item("create_mininglaser:drill_core_t1"));
        scene.overlay().showText(60).attachKeyFrame()
                .text("Right-click the Controller with a Drill Core to insert it.")
                .pointAt(controller.getCenter())
                .placeNearTarget();
        scene.idle(65);

        scene.overlay()
                .showControls(util.vector().topOf(CONTROLLER_POS).add(0, 0.25, 0), Pointing.RIGHT, 60)
                .rightClick()
                .withItem(item("create:wrench"));
        scene.overlay().showText(60).attachKeyFrame()
                .text("Right-click with a Wrench to remove the Core.")
                .pointAt(controller.getCenter())
                .placeNearTarget();
        scene.idle(65);
    }

    public static void outputSetup(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("output_setup", "Connecting the Output Hatch");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        makeFloor(scene, util);

        Selection hatch = util.select().position(HATCH_POS);

        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.idle(10);

        scene.rotateCameraY(180);
        scene.idle(10);

        scene.overlay().showOutline(PonderPalette.BLUE, hatch, hatch, 60);
        scene.overlay().showText(60).attachKeyFrame()
                .text("Items are output via the Hatch.")
                .pointAt(hatch.getCenter())
                .placeNearTarget();
        scene.idle(65);

        scene.overlay()
                .showControls(util.vector().topOf(HATCH_POS).add(0, 0.25, 0), Pointing.RIGHT, 60)
                .rightClick()
                .withItem(item("create:andesite_funnel"));
        scene.overlay().showText(60).attachKeyFrame()
                .text("Attach a funnel/belt/chest here to collect items.")
                .pointAt(hatch.getCenter())
                .placeNearTarget();
        scene.idle(65);
    }
}