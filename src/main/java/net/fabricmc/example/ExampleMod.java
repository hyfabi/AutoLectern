package net.fabricmc.example;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.example.commands.AutoLec;
import net.fabricmc.example.commands.ClientCommandManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.ArrayList;

public class ExampleMod implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.


    public static File configDir;
    public static final ExampleMod INSTANCE = new ExampleMod();
    public static boolean ALstart = false;
    public static boolean ALstop = false;
    public static boolean ALitemsync = false;
    public static boolean ALhasitemdropped = false;
    public static Integer UUID = 1;
    public boolean ALissneak = false;
    public static boolean ALvillupdate = false;
    public static boolean ALofferupdate = false;
    public static VillagerEntity yevilldatgotupdated;
    public static NewVillagerInfo NVI = new NewVillagerInfo(null, 999);
    public static ArrayList<ALGoal> ALcurgoal = new ArrayList<>();
    public static boolean ALdotrackpro = false;
    public static boolean ALdotracktrades = false;
    public long stage4start = 0;
    public long stage3start = 0;
    public static int stage = 0;
    public double stageayaw = 0;
    public double stageapitch = 0;
    public Vec3d plroripos = Vec3d.ZERO;
    public int bfs = -1;
    public Direction lecside = Direction.SOUTH;
    public static BlockPos lecloc = null;

    public ExampleMod() {
    }

    public Integer getCheapestVE(Enchantment ve) {
        Integer ret = (ve.getMaxLevel() * 3) + 2;
        if (ve.isTreasure()) {
            ret *= 2;
        }
        if (ret > 64)
            return 64;
        return ret;
    }

    static final Text startmessage = Text.literal("[Auto Lectern] ").formatted(Formatting.YELLOW).append(Text.literal("Started").formatted(Formatting.GREEN));
    static final Text stoppingmessage = Text.literal("[Auto Lectern] ").formatted(Formatting.YELLOW).append(Text.literal("Stopping...").formatted(Formatting.RED));
    static final Text stoppedmessage = Text.literal("[Auto Lectern] ").formatted(Formatting.YELLOW).append(Text.literal("Stopped.").formatted(Formatting.RED));
    static final Text alreadystopmessage = Text.literal("[Auto Lectern] ").formatted(Formatting.YELLOW).append(Text.literal("Already stopped.").formatted(Formatting.RED));
    static final Text pleaselookmessage = Text.literal("[Auto Lectern] ").formatted(Formatting.YELLOW).append(Text.literal("Please look at a lectern before running this command.").formatted(Formatting.RED));
    static final Text completedmessage = Text.literal("[Auto Lectern] ").formatted(Formatting.YELLOW).append(Text.literal("Completed.").formatted(Formatting.GREEN));

    public void MinecraftTickHead(MinecraftClient mc) {
        if (ALstart) {
            if (mc.world == null || mc.interactionManager == null) return;
            bfs = -1;
            if (mc.player != null && mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK && mc.world.getBlockState(((BlockHitResult) mc.crosshairTarget).getBlockPos()).getBlock() == Blocks.LECTERN) {
                stageayaw = mc.player.getYaw();
                stageapitch = mc.player.getPitch();
                plroripos = mc.player.getPos();
                lecloc = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
                lecside = ((BlockHitResult) mc.crosshairTarget).getSide();
                if (mc.player.isInSneakingPose()) {
                    ALissneak = true;
                }
                mc.inGameHud.getChatHud().addMessage(startmessage);
                ALhasitemdropped = false;
                stage = 1;
            } else {
                mc.inGameHud.getChatHud().addMessage(pleaselookmessage);
            }
            ALstart = false;
        }
        if (ALstop) {
            mc.inGameHud.getChatHud().addMessage(stoppingmessage);
            if (stage == 0) {
                mc.inGameHud.getChatHud().addMessage(alreadystopmessage);
                ALstop = false;
            }
        }
        if (stage != 0) {
            if (mc.world == null || mc.interactionManager == null || mc.player == null) {
                stage = 0;
                return;
            }
        }
        if (stage == 1) {
            mc.options.forwardKey.setPressed(false);
            mc.options.backKey.setPressed(false);
            mc.options.leftKey.setPressed(false);
            mc.options.rightKey.setPressed(false);
            mc.options.sneakKey.setPressed(ALissneak);
            mc.player.setPosition(plroripos);
            if (mc.currentScreen instanceof MerchantScreen) {
                mc.player.closeHandledScreen();
            }
            if (bfs != -1 && mc.player.getOffHandStack().getItem() != Items.LECTERN) {
                mc.player.getInventory().selectedSlot = bfs;
            }

            if (mc.world.getBlockState(lecloc).getBlock() == Blocks.AIR) {
                //System.out.println("=> stage 2");
                if (!ALitemsync || ALhasitemdropped) {
                    stage = 2;
                    mc.options.attackKey.setPressed(false);
                }
            } else {
                mc.player.setYaw((float) stageayaw);
                mc.player.setPitch((float) stageapitch);
                mc.options.attackKey.setPressed(true);
                mc.interactionManager.updateBlockBreakingProgress(lecloc, lecside);
            }
            if (ALstop) {
                mc.interactionManager.cancelBlockBreaking();
                ALstop = false;
                mc.options.attackKey.setPressed(false);
                stage = 0;
                mc.inGameHud.getChatHud().addMessage(stoppedmessage);
            }
        } else if (stage == 2) {
            if (mc.player.getOffHandStack().getItem() != Items.LECTERN) {
                PlayerInventory INV = mc.player.getInventory();
                if (INV.getStack(INV.selectedSlot).getItem() != Items.LECTERN) {
                    for (int i = 0; i < 9; i++) {
                        if (INV.getStack(i).getItem() == Items.LECTERN) {
                            bfs = INV.selectedSlot;
                            INV.selectedSlot = i;
                            break;
                        }
                    }
                }
            }
            mc.options.forwardKey.setPressed(false);
            mc.options.backKey.setPressed(false);
            mc.options.leftKey.setPressed(false);
            mc.options.rightKey.setPressed(false);
            mc.options.sneakKey.setPressed(ALissneak);
            mc.player.setPosition(plroripos);
            Block lecLocBlock = mc.world.getBlockState(lecloc).getBlock();
            if (lecLocBlock != Blocks.LECTERN) {
                if (lecLocBlock != Blocks.AIR) {
                    mc.options.useKey.setPressed(false);
                    //System.out.println("=> stage 1");
                    ALhasitemdropped = false;
                    stage = 1;
                } else {
                    mc.player.setYaw((float) stageayaw);
                    mc.player.setPitch((float) stageapitch);
                    mc.options.useKey.setPressed(true);
                    if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                        mc.interactionManager.interactBlock(mc.player, mc.player.getOffHandStack().getItem() == Items.LECTERN ? Hand.OFF_HAND : Hand.MAIN_HAND, (BlockHitResult) mc.crosshairTarget);
                    }
                }
            } else {
                mc.options.useKey.setPressed(false);
                //System.out.println("=> stage 3");
                stage = 3;
                ALdotrackpro = true;
                stage3start = System.currentTimeMillis();
            }
            if (ALstop) {
                ALstop = false;
                mc.options.useKey.setPressed(false);
                stage = 0;
                mc.inGameHud.getChatHud().addMessage(stoppedmessage);
            }
        } else if (stage == 3) {
            if (mc.world.getBlockState(lecloc).getBlock() != Blocks.LECTERN) {
                stage = 2;
            } else {
                mc.options.forwardKey.setPressed(false);
                mc.options.backKey.setPressed(false);
                mc.options.leftKey.setPressed(false);
                mc.options.rightKey.setPressed(false);
                mc.options.sneakKey.setPressed(ALissneak);
                mc.player.setPosition(plroripos);
                boolean ischatscreen = false;
                if (mc.currentScreen instanceof ChatScreen) {
                    ischatscreen = true;
                    stage3start = System.currentTimeMillis();
                }
                if (ALvillupdate && !ischatscreen) {

                    //System.out.println("=> stage 4");
                    mc.interactionManager.interactEntity(mc.player, yevilldatgotupdated, Hand.MAIN_HAND);
                    stage = 4;
                    ALdotracktrades = true;
                    ALvillupdate = false;
                    ALdotrackpro = false;
                    stage4start = System.currentTimeMillis();
                }
                if ((System.currentTimeMillis() - stage3start) >= 3000) {
                    //System.out.println("=> stage 1");
                    ALdotrackpro = false;
                    ALhasitemdropped = false;
                    stage = 1;
                }
                if (ALstop) {
                    ALdotrackpro = false;
                    ALstop = false;
                    stage = 0;
                    mc.inGameHud.getChatHud().addMessage(stoppedmessage);
                }
            }
        } else if (stage == 4) {
            mc.options.forwardKey.setPressed(false);
            mc.options.backKey.setPressed(false);
            mc.options.leftKey.setPressed(false);
            mc.options.rightKey.setPressed(false);
            mc.options.sneakKey.setPressed(ALissneak);
            mc.player.setPosition(plroripos);
            if (ALofferupdate) {
                ALofferupdate = false;
                if (mc.currentScreen instanceof MerchantScreen) {
                    mc.player.closeHandledScreen();
                }
                if (ALcurgoal.isEmpty()) {
                    if (NVI.VE != null) {
                        ALdotracktrades = false;
                        stage = 0;
                        mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1));
                        GLFW.glfwRequestWindowAttention(mc.getWindow().getHandle());
                        mc.inGameHud.getChatHud().addMessage(completedmessage);
                    } else {
                        //System.out.println("=> stage 1");
                        ALdotracktrades = false;
                        ALhasitemdropped = false;
                        stage = 1;
                    }
                } else {
                    for (ALGoal alg : ALcurgoal) {
                        if (alg.enchant == NVI.VE && (alg.type == 0 ||
                                (alg.type == 1 && getCheapestVE(NVI.VE).equals(NVI.price)) ||
                                (alg.type == 3 && NVI.price >= alg.min && NVI.price <= alg.max))) {
                            ALdotracktrades = false;
                            stage = 0;
                            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1));
                            GLFW.glfwRequestWindowAttention(mc.getWindow().getHandle());
                            mc.inGameHud.getChatHud().addMessage(completedmessage);
                            break;
                        }
                    }
                    if (stage != 0) {
                        //System.out.println("=> stage 1");
                        ALdotracktrades = false;
                        ALhasitemdropped = false;
                        stage = 1;
                    }
                }

            }
            if ((System.currentTimeMillis() - stage4start) >= 3000) {
                //System.out.println("=> stage 1");
                ALdotracktrades = false;
                ALhasitemdropped = false;
                stage = 1;
            }
            if (ALstop) {
                ALdotracktrades = false;
                ALstop = false;
                stage = 0;
                mc.inGameHud.getChatHud().addMessage(stoppedmessage);
            }
        }
    }

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        ClientCommandManager.clearClientSideCommands();
        AutoLec.register(dispatcher);

    }

    @Override
    public void onInitialize() {
        configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), "autolectern");
        //noinspection ResultOfMethodCallIgnored
        configDir.mkdirs();
    }
}
