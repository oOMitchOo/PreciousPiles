package oomitchoo.preciouspiles.common.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import oomitchoo.preciouspiles.block.StackedIngotsBlock;
import org.joml.Matrix4f;

import java.util.function.BiConsumer;

//@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class PlacementHighlightEvent {

    @SubscribeEvent
    public static void onHighlightStackedIngotsBlock(RenderHighlightEvent.Block event) {
        Camera camera = event.getCamera();
        Entity player = camera.getEntity();
        //(Player) player.getLookAngle();
        //(Player) player.getEyePosition()
        HitResult hitResult = event.getTarget();
        //hitResult.getLocation() Gibt mir den genauen Treffpunkt des Spielerblicks

        if(player instanceof Player) {
            if(hitResult instanceof BlockHitResult blockHitResult && hitResult.getType() != HitResult.Type.MISS) {
                BlockPos posLookingAt = blockHitResult.getBlockPos();
                BlockPos posLookingIn = posLookingAt.relative(blockHitResult.getDirection());
                Level level = player.getCommandSenderWorld();
                BlockState targetBlock = level.getBlockState(posLookingAt);

                if (targetBlock.getBlock() instanceof StackedIngotsBlock) {
                    //todo: checks, ob der Spieler sneakt und ein richtiges Item in der Hand hat?
                    Vec3 cameraPos = event.getCamera().getPosition();
                    PoseStack poseStack = event.getPoseStack();
                    AABB aabb = new AABB(posLookingAt);
                    MultiBufferSource buffer = event.getMultiBufferSource();

                    drawBoxOutline(hitResult.getLocation(), poseStack, buffer, (float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z, aabb, 1.0f, 0f, 0f, 1.0f);
                } else if (level.getBlockState(posLookingIn).getBlock() instanceof StackedIngotsBlock) {
                    //todo: checks, ob der Spieler sneakt und ein richtiges Item in der Hand hat?
                    Vec3 cameraPos = event.getCamera().getPosition();
                    PoseStack poseStack = event.getPoseStack();
                    AABB aabb = new AABB(posLookingIn);
                    MultiBufferSource buffer = event.getMultiBufferSource();

                    drawBoxOutline(hitResult.getLocation(), poseStack, buffer, (float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z, aabb, 1.0f, 0f, 0f, 1.0f);
                }
            }
        }
    }

    private static void drawBoxOutline(Vec3 exactLookingPoint, PoseStack poseStack, MultiBufferSource bufferSource, float camX, float camY, float camZ, AABB box, float red, float green, float blue, float alpha) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();
        PoseStack.Pose pose = poseStack.last();
        double x1 = box.minX - camX;
        double y1 = box.minY - camY;
        double z1 = box.minZ - camZ;
        double x2 = box.maxX - camX;
        double y2 = box.maxY - camY;
        double z2 = box.maxZ - camZ;

        if(exactLookingPoint.x - box.minX < 0.5) {
            x2 = x2 - 0.5;
        } else { x1 = x1 + 0.5; }
        if(exactLookingPoint.y - box.minY < 0.5) {
            y2 = y2 - 0.5;
        } else { y1 = y1 + 0.5; }
        if(exactLookingPoint.z - box.minZ < 0.5) {
            z2 = z2 - 0.5;
        } else { z1 = z1 + 0.5; }

        BiConsumer<Vec3, Vec3> line = (start, end) -> {
            buffer.addVertex(matrix, (float) start.x, (float) start.y, (float) start.z)
                    .setNormal(pose, 0.0f, 1.0f, 0.0f)
                    .setColor(red, green, blue, alpha);

            buffer.addVertex(matrix, (float) end.x, (float) end.y, (float) end.z)
                    .setNormal(pose, 0.0f, 1.0f, 0.0f)
                    .setColor(red, green, blue, alpha);
        };

        // Kanten der Box
        line.accept(new Vec3(x1, y1, z1), new Vec3(x2, y1, z1));
        line.accept(new Vec3(x2, y1, z1), new Vec3(x2, y1, z2));
        line.accept(new Vec3(x2, y1, z2), new Vec3(x1, y1, z2));
        line.accept(new Vec3(x1, y1, z2), new Vec3(x1, y1, z1));

        line.accept(new Vec3(x1, y2, z1), new Vec3(x2, y2, z1));
        line.accept(new Vec3(x2, y2, z1), new Vec3(x2, y2, z2));
        line.accept(new Vec3(x2, y2, z2), new Vec3(x1, y2, z2));
        line.accept(new Vec3(x1, y2, z2), new Vec3(x1, y2, z1));

        line.accept(new Vec3(x1, y1, z1), new Vec3(x1, y2, z1));
        line.accept(new Vec3(x2, y1, z1), new Vec3(x2, y2, z1));
        line.accept(new Vec3(x2, y1, z2), new Vec3(x2, y2, z2));
        line.accept(new Vec3(x1, y1, z2), new Vec3(x1, y2, z2));
    }

    private static void drawBoxOutline(BlockState targetBlock, Vec3 exactLookingPoint, PoseStack poseStack, MultiBufferSource bufferSource, float camX, float camY, float camZ, AABB box, float red, float green, float blue, float alpha) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();
        PoseStack.Pose pose = poseStack.last();
        double x1 = box.minX - camX;
        double y1 = box.minY - camY;
        double z1 = box.minZ - camZ;
        double x2 = box.maxX - camX;
        double y2 = box.maxY - camY;
        double z2 = box.maxZ - camZ;

        boolean lookingAtOrInLowerPart;
        boolean lookingAtOrInWesternPart;

        if(exactLookingPoint.y - box.minY < 0.5) {
            y2 = y2 - 0.5;
        } else { y1 = y1 + 0.5; }
        if(exactLookingPoint.x - box.minX < 0.5) {
            x2 = x2 - 0.5;
        } else { x1 = x1 + 0.5; }
        if(exactLookingPoint.z - box.minZ < 0.5) {
            z2 = z2 - 0.5;
        } else {
            z1 = z1 + 0.5;
        }

        BiConsumer<Vec3, Vec3> line = (start, end) -> {
            buffer.addVertex(matrix, (float) start.x, (float) start.y, (float) start.z)
                    .setNormal(pose, 0.0f, 1.0f, 0.0f)
                    .setColor(red, green, blue, alpha);

            buffer.addVertex(matrix, (float) end.x, (float) end.y, (float) end.z)
                    .setNormal(pose, 0.0f, 1.0f, 0.0f)
                    .setColor(red, green, blue, alpha);
        };

        // Kanten der Box
        line.accept(new Vec3(x1, y1, z1), new Vec3(x2, y1, z1));
        line.accept(new Vec3(x2, y1, z1), new Vec3(x2, y1, z2));
        line.accept(new Vec3(x2, y1, z2), new Vec3(x1, y1, z2));
        line.accept(new Vec3(x1, y1, z2), new Vec3(x1, y1, z1));

        line.accept(new Vec3(x1, y2, z1), new Vec3(x2, y2, z1));
        line.accept(new Vec3(x2, y2, z1), new Vec3(x2, y2, z2));
        line.accept(new Vec3(x2, y2, z2), new Vec3(x1, y2, z2));
        line.accept(new Vec3(x1, y2, z2), new Vec3(x1, y2, z1));

        line.accept(new Vec3(x1, y1, z1), new Vec3(x1, y2, z1));
        line.accept(new Vec3(x2, y1, z1), new Vec3(x2, y2, z1));
        line.accept(new Vec3(x2, y1, z2), new Vec3(x2, y2, z2));
        line.accept(new Vec3(x1, y1, z2), new Vec3(x1, y2, z2));
    }
}
