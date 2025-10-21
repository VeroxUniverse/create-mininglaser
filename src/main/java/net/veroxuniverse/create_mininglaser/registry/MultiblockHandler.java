package net.veroxuniverse.create_mininglaser.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.veroxuniverse.create_mininglaser.content.blocks.LaserDrillCasingBlock;
import net.veroxuniverse.create_mininglaser.content.blocks.LaserDrillControllerBlock;
import net.veroxuniverse.create_mininglaser.content.blocks.LaserDrillControllerBlockEntity;
import net.veroxuniverse.create_mininglaser.content.blocks.LaserDrillHatchBlock;

import java.util.ArrayList;
import java.util.List;

public class MultiblockHandler {

    private record PV(BlockPos offset, DrillCasingVariant variant) {}

    public static boolean tryFormDrill(Level level, BlockPos controllerPos, BlockState controllerState, Player player) {
        if (controllerState.getBlock() instanceof LaserDrillControllerBlock
                && controllerState.hasProperty(LaserDrillControllerBlock.ACTIVE)
                && controllerState.getValue(LaserDrillControllerBlock.ACTIVE)) {
            return false;
        }

        Direction sideDir = null;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos check = controllerPos.relative(dir);
            Block block = level.getBlockState(check).getBlock();
            if (block instanceof LaserDrillHatchBlock) {
                sideDir = dir;
                break;
            }
        }
        if (sideDir == null) return false;

        Direction forwardDir = rotateCCW(sideDir);
        Direction rightDir   = sideDir;

        List<PV> layout = buildLayout(forwardDir, rightDir);
        BlockPos hatchOffset = new BlockPos(rightDir.getStepX(), 0, rightDir.getStepZ());

        for (PV pv : layout) {
            BlockPos p = controllerPos.offset(pv.offset());
            if (!(level.getBlockState(p).getBlock() instanceof LaserDrillCasingBlock))
                return false;
        }
        if (!(level.getBlockState(controllerPos.offset(hatchOffset)).getBlock() instanceof LaserDrillHatchBlock))
            return false;

        updateModels(level, controllerPos, layout, hatchOffset, forwardDir);

        BlockState ctrlUpdated = controllerState
                .setValue(LaserDrillControllerBlock.HORIZONTAL_FACING, forwardDir)
                .setValue(LaserDrillControllerBlock.ACTIVE, true);
        level.setBlock(controllerPos, ctrlUpdated, 3);
        level.sendBlockUpdated(controllerPos, controllerState, ctrlUpdated, 2);

        return true;
    }

    private static List<PV> buildLayout(Direction forwardDir, Direction rightDir) {
        List<PV> out = new ArrayList<>(11);

        out.add(new PV(localToWorld(+ -1, -1, +1, forwardDir, rightDir), DrillCasingVariant.BOTTOM_NW));
        out.add(new PV(localToWorld(   0, -1, +1, forwardDir, rightDir), DrillCasingVariant.BOTTOM_N));
        out.add(new PV(localToWorld(  +1, -1, +1, forwardDir, rightDir), DrillCasingVariant.BOTTOM_NE));

        out.add(new PV(localToWorld(  -1, -1,  0, forwardDir, rightDir), DrillCasingVariant.BOTTOM_W));
        out.add(new PV(localToWorld(  +1, -1,  0, forwardDir, rightDir), DrillCasingVariant.BOTTOM_E));

        out.add(new PV(localToWorld(  -1, -1, -1, forwardDir, rightDir), DrillCasingVariant.BOTTOM_SW));
        out.add(new PV(localToWorld(   0, -1, -1, forwardDir, rightDir), DrillCasingVariant.BOTTOM_S));
        out.add(new PV(localToWorld(  +1, -1, -1, forwardDir, rightDir), DrillCasingVariant.BOTTOM_SE));

        out.add(new PV(localToWorld(  -1,  0,  0, forwardDir, rightDir), DrillCasingVariant.TOP_LEFT));
        out.add(new PV(localToWorld(   0,  0, +1, forwardDir, rightDir), DrillCasingVariant.TOP_FRONT));
        out.add(new PV(localToWorld(   0,  0, -1, forwardDir, rightDir), DrillCasingVariant.TOP_BACK));

        return out;
    }

    private static BlockPos localToWorld(int r, int y, int f, Direction forwardDir, Direction rightDir) {
        int fx = forwardDir.getStepX(), fz = forwardDir.getStepZ();
        int rx = rightDir.getStepX(),   rz = rightDir.getStepZ();
        int dx = r * rx + f * fx;
        int dz = r * rz + f * fz;
        return new BlockPos(dx, y, dz);
    }

    private static Direction rotateCCW(Direction d) {
        return switch (d) {
            case NORTH -> Direction.WEST;
            case WEST  -> Direction.SOUTH;
            case SOUTH -> Direction.EAST;
            case EAST  -> Direction.NORTH;
            default    -> d;
        };
    }

    private static Direction rotateCW(Direction d) {
        return switch (d) {
            case NORTH -> Direction.EAST;
            case EAST  -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST  -> Direction.NORTH;
            default    -> d;
        };
    }

    private static void updateModels(Level level,
                                     BlockPos controllerPos,
                                     List<PV> casingPV,
                                     BlockPos hatchOffset,
                                     Direction forwardDir) {

        Direction renderDir = forwardDir;

        for (PV pv : casingPV) {
            BlockPos pos = controllerPos.offset(pv.offset());
            BlockState old = level.getBlockState(pos);
            if (old.getBlock() instanceof LaserDrillCasingBlock) {
                BlockState updated = old
                        .setValue(LaserDrillCasingBlock.FORMED, true)
                        .setValue(LaserDrillCasingBlock.FACING, renderDir)
                        .setValue(LaserDrillCasingBlock.VARIANT, pv.variant());
                level.setBlock(pos, updated, 3);
                level.sendBlockUpdated(pos, old, updated, 2);
            }
        }

        BlockPos hatchPos = controllerPos.offset(hatchOffset);
        BlockState oldHatch = level.getBlockState(hatchPos);
        if (oldHatch.getBlock() instanceof LaserDrillHatchBlock) {
            BlockState updated = oldHatch
                    .setValue(LaserDrillHatchBlock.FACING, renderDir)
                    .setValue(LaserDrillHatchBlock.ACTIVE, true);
            level.setBlock(hatchPos, updated, 3);
            level.sendBlockUpdated(hatchPos, oldHatch, updated, 2);
        }
    }

    public static void unformAtController(Level level, BlockPos controllerPos) {
        BlockState ctrlState = level.getBlockState(controllerPos);
        unformAtController(level, controllerPos, ctrlState, true);
    }

    public static void unformAtController(Level level, BlockPos controllerPos, BlockState ctrlState) {
        unformAtController(level, controllerPos, ctrlState, true);
    }

    public static void unformAtController(Level level, BlockPos controllerPos, BlockState ctrlState, boolean updateControllerBlock) {
        if (!(ctrlState.getBlock() instanceof LaserDrillControllerBlock)) {
            boolean anyOrientationHasCasing = false;
            for (Direction fwd : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
                Direction rightDir = rotateCW(fwd);
                List<PV> layout = buildLayout(fwd, rightDir);
                if (isAnyCasingPresent(level, controllerPos, layout)) {
                    anyOrientationHasCasing = true;
                    break;
                }
            }
            if (!anyOrientationHasCasing) return;
            for (Direction fwd : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
                Direction rightDir = rotateCW(fwd);
                List<PV> layout = buildLayout(fwd, rightDir);
                for (PV pv : layout) {
                    BlockPos p = controllerPos.offset(pv.offset());
                    BlockState old = level.getBlockState(p);
                    if (old.getBlock() instanceof LaserDrillCasingBlock) {
                        BlockState updated = old.setValue(LaserDrillCasingBlock.FORMED, false);
                        level.setBlock(p, updated, 3);
                        level.sendBlockUpdated(p, old, updated, 2);
                    }
                }
            }
            return;
        }

        Direction sideDir = null;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(controllerPos.relative(d)).getBlock() instanceof LaserDrillHatchBlock) {
                sideDir = d;
                break;
            }
        }

        if (sideDir != null) {
            Direction forwardDir = rotateCCW(sideDir);
            Direction rightDir   = sideDir;

            if (updateControllerBlock) {
                if (isStructureIntact(level, controllerPos, forwardDir, rightDir)) return;
            } else {
                if (!isStructureIntact(level, controllerPos, forwardDir, rightDir)) return;
            }

            List<PV> layout = buildLayout(forwardDir, rightDir);
            for (PV pv : layout) {
                BlockPos p = controllerPos.offset(pv.offset());
                BlockState old = level.getBlockState(p);
                if (old.getBlock() instanceof LaserDrillCasingBlock) {
                    BlockState updated = old.setValue(LaserDrillCasingBlock.FORMED, false);
                    level.setBlock(p, updated, 3);
                    level.sendBlockUpdated(p, old, updated, 2);
                }
            }
            BlockPos hatchPos = controllerPos.relative(rightDir);
            BlockState oldHatch = level.getBlockState(hatchPos);
            if (oldHatch.getBlock() instanceof LaserDrillHatchBlock) {
                BlockState updated = oldHatch.setValue(LaserDrillHatchBlock.ACTIVE, false);
                level.setBlock(hatchPos, updated, 3);
                level.sendBlockUpdated(hatchPos, oldHatch, updated, 2);
            }
        } else {
            Direction forwardFromState = ctrlState.hasProperty(LaserDrillControllerBlock.HORIZONTAL_FACING)
                    ? ctrlState.getValue(LaserDrillControllerBlock.HORIZONTAL_FACING) : null;
            if (forwardFromState == null) return;

            Direction rightDir = rotateCW(forwardFromState);

            if (updateControllerBlock) {
                if (isStructureIntact(level, controllerPos, forwardFromState, rightDir)) return;
            } else {
                if (!isStructureIntact(level, controllerPos, forwardFromState, rightDir)) return;
            }

            List<PV> layout = buildLayout(forwardFromState, rightDir);
            for (PV pv : layout) {
                BlockPos p = controllerPos.offset(pv.offset());
                BlockState old = level.getBlockState(p);
                if (old.getBlock() instanceof LaserDrillCasingBlock) {
                    BlockState updated = old.setValue(LaserDrillCasingBlock.FORMED, false);
                    level.setBlock(p, updated, 3);
                    level.sendBlockUpdated(p, old, updated, 2);
                }
            }
        }

        if (updateControllerBlock && ctrlState.getBlock() instanceof LaserDrillControllerBlock) {
            boolean shouldDeactivate = true;

            Direction checkSide = null;
            for (Direction d : Direction.Plane.HORIZONTAL) {
                if (level.getBlockState(controllerPos.relative(d)).getBlock() instanceof LaserDrillHatchBlock) {
                    checkSide = d;
                    break;
                }
            }
            if (checkSide != null) {
                Direction forwardDir = rotateCCW(checkSide);
                Direction rightDir   = checkSide;
                if (isStructureIntact(level, controllerPos, forwardDir, rightDir)) {
                    shouldDeactivate = false;
                }
            } else {
                Direction forwardFromState = ctrlState.hasProperty(LaserDrillControllerBlock.HORIZONTAL_FACING)
                        ? ctrlState.getValue(LaserDrillControllerBlock.HORIZONTAL_FACING) : null;
                if (forwardFromState != null) {
                    Direction rightDir = rotateCW(forwardFromState);
                    if (isStructureIntact(level, controllerPos, forwardFromState, rightDir)) {
                        shouldDeactivate = false;
                    }
                }
            }

            if (shouldDeactivate) {
                BlockState updatedCtrl = ctrlState.setValue(LaserDrillControllerBlock.ACTIVE, false);
                BlockEntity be = level.getBlockEntity(controllerPos);
                if (be instanceof LaserDrillControllerBlockEntity drill) {
                    drill.ejectCoreUp();
                }
                level.setBlock(controllerPos, updatedCtrl, 3);
                level.sendBlockUpdated(controllerPos, ctrlState, updatedCtrl, 2);
            }
        }
    }

    private static boolean isAnyCasingPresent(Level level, BlockPos controllerPos, List<PV> layout) {
        for (PV pv : layout) {
            BlockPos p = controllerPos.offset(pv.offset());
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof LaserDrillCasingBlock) return true;
        }
        return false;
    }

    private static boolean isStructureIntact(Level level, BlockPos controllerPos, Direction forwardDir, Direction rightDir) {
        List<PV> layout = buildLayout(forwardDir, rightDir);
        for (PV pv : layout) {
            BlockPos p = controllerPos.offset(pv.offset());
            BlockState s = level.getBlockState(p);
            if (!(s.getBlock() instanceof LaserDrillCasingBlock)) return false;
        }
        BlockPos hatchPos = controllerPos.relative(rightDir);
        BlockState hs = level.getBlockState(hatchPos);
        return (hs.getBlock() instanceof LaserDrillHatchBlock);
    }

    public static void unformNear(Level level, BlockPos brokenPos, BlockState oldState) {
        BlockState brokenState = oldState != null ? oldState : level.getBlockState(brokenPos);
        boolean isCasing = brokenState.getBlock() instanceof LaserDrillCasingBlock;
        boolean isHatch  = brokenState.getBlock() instanceof LaserDrillHatchBlock;

        if (!isCasing && !isHatch) return;

        if (isCasing &&
                brokenState.hasProperty(LaserDrillCasingBlock.FORMED) &&
                !brokenState.getValue(LaserDrillCasingBlock.FORMED)) {
            return;
        }

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos controller = brokenPos.offset(dx, dy, dz);
                    BlockState s = level.getBlockState(controller);
                    if (!(s.getBlock() instanceof LaserDrillControllerBlock)) continue;
                    if (!s.hasProperty(LaserDrillControllerBlock.ACTIVE) || !s.getValue(LaserDrillControllerBlock.ACTIVE))
                        continue;

                    if (isHatch) {
                        Direction side = null;
                        for (Direction d : Direction.Plane.HORIZONTAL) {
                            if (controller.relative(d).equals(brokenPos)) { side = d; break; }
                        }
                        if (side == null) continue;
                        Direction fwd = rotateCCW(side);
                        Direction right = side;
                        List<PV> layout = buildLayout(fwd, right);
                        for (PV pv : layout) {
                            BlockPos p = controller.offset(pv.offset());
                            BlockState old = level.getBlockState(p);
                            if (old.getBlock() instanceof LaserDrillCasingBlock) {
                                BlockState updated = old.setValue(LaserDrillCasingBlock.FORMED, false);
                                level.setBlock(p, updated, 3);
                                level.sendBlockUpdated(p, old, updated, 2);
                            }
                        }
                        BlockState ctrlSt = level.getBlockState(controller);
                        if (ctrlSt.getBlock() instanceof LaserDrillControllerBlock) {
                            BlockState updatedCtrl = ctrlSt.setValue(LaserDrillControllerBlock.ACTIVE, false);
                            BlockEntity be = level.getBlockEntity(controller);
                            if (be instanceof LaserDrillControllerBlockEntity drill) {
                                drill.ejectCoreUp();
                            }
                            level.setBlock(controller, updatedCtrl, 3);
                            level.sendBlockUpdated(controller, ctrlSt, updatedCtrl, 2);
                        }
                        return;
                    }

                    Direction sideDir = null;
                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        if (level.getBlockState(controller.relative(dir)).getBlock() instanceof LaserDrillHatchBlock) {
                            sideDir = dir;
                            break;
                        }
                    }
                    if (sideDir == null) continue;

                    Direction forward = rotateCCW(sideDir);
                    Direction right   = sideDir;

                    var layout = buildLayout(forward, right);
                    boolean isMember = false;
                    for (var pv : layout) {
                        BlockPos member = controller.offset(pv.offset());
                        if (member.equals(brokenPos)) { isMember = true; break; }
                    }
                    BlockPos hatchPos = controller.relative(right);
                    if (hatchPos.equals(brokenPos)) isMember = true;

                    if (isMember) {
                        unformAtController(level, controller, s, true);
                        return;
                    }
                }
            }
        }
    }

    public static void unformNear(Level level, BlockPos brokenPos) {
        unformNear(level, brokenPos, null);
    }

}