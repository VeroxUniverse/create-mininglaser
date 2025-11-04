package net.veroxuniverse.create_mininglaser.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.Direction;

public class MiningLaserPonderScenes {

    public static void laserMultiblock(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("laser_multiblock", "Building the Laser Drill Multiblock");
        scene.configureBasePlate(0, 0, 5); // 5x5 Baseplate
        scene.showBasePlate();

        Selection controller = util.select().position(2, 1, 2);
        Selection hatch      = util.select().position(2, 1, 1);
        Selection layer0     = util.select().layer(0);

        scene.idle(15);
        scene.world().showSection(layer0, Direction.UP);
        scene.idle(20);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Place 11 Drill Casings, 1 Hatch, and 1 Controller.")
                .pointAt(controller.getCenter())
                .placeNearTarget();

        scene.idle(70);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Use a Wrench on the Controller to form the multiblock.")
                .pointAt(controller.getCenter())
                .placeNearTarget();

        scene.idle(70);

        scene.overlay().showText(60)
                .attachKeyFrame()
                .text("The Hatch must be on the side to output items.")
                .pointAt(hatch.getCenter())
                .placeNearTarget();

        scene.idle(60);
    }

    public static void insertCore(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("insert_core", "Inserting and Removing Drill Cores");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        Selection controller = util.select().position(2, 1, 2);
        scene.idle(15);
        scene.world().showSection(controller, Direction.DOWN);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Right-click the Controller with a Drill Core to insert it.")
                .pointAt(controller.getCenter())
                .placeNearTarget();
        scene.idle(70);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Right-click with a Wrench to remove the Core.")
                .pointAt(controller.getCenter())
                .placeNearTarget();
        scene.idle(70);
    }

    public static void outputSetup(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("output_setup", "Connecting the Output Hatch");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        Selection hatch = util.select().position(2, 1, 1);
        scene.idle(15);
        scene.world().showSection(hatch, Direction.DOWN);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Items are output via the Hatch.")
                .pointAt(hatch.getCenter())
                .placeNearTarget();
        scene.idle(70);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Attach a belt, funnel, or chest here to collect items.")
                .pointAt(hatch.getCenter())
                .placeNearTarget();
        scene.idle(70);
    }
}
