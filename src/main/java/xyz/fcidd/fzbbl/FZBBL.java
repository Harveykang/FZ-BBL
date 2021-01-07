package xyz.fcidd.fzbbl;

import java.util.ArrayList;
import java.util.HashMap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.world.World;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import xyz.fcidd.fzbbl.callback.PistonBreakBedrockCallback;
import xyz.fcidd.fzbbl.callback.PlayerPlacedCallback;
import xyz.fcidd.fzbbl.callback.ScoreboardAddedCallback;
import xyz.fcidd.fzbbl.callback.ScoreboardRemovedCallback;

public class FZBBL implements ModInitializer {
	public static HashMap<World, HashMap<BlockPos, PlayerEntity>> pistonCaches;
	private static HashMap<BlockPos, PlayerEntity> pistonPosCaches;
	private static ArrayList<ScoreboardObjective> scoreboardBBL = new ArrayList<>();

	@Override
	public void onInitialize() {
		ScoreboardAddedCallback.EVENT.register((name, criterion, displayName, renderType, objective) -> {
			try {
				if (name.substring(name.length() - 4).equals(".bbl") && criterion.equals(ScoreboardCriterion.DUMMY)) {
					System.out.println("AddedBedrockBreakedList: " + name);
					scoreboardBBL.add(objective);
				}
			} catch (StringIndexOutOfBoundsException e) {

			}
			return ActionResult.PASS;
		});
		ScoreboardRemovedCallback.EVENT.register((objective) -> {
			String name = objective.getName();
			try {
				if (name.substring(name.length() - 4).equals(".bbl") && objective.getCriterion().equals(ScoreboardCriterion.DUMMY)) {
					System.out.println("RemovedBedrockBreakedList: " + name);
					scoreboardBBL.remove(objective);
				}
			} catch (StringIndexOutOfBoundsException e) {

			}
			return ActionResult.PASS;
		});
		PlayerPlacedCallback.EVENT.register((player, world, stack, hand, hitResult) -> {
			if (stack.getItem().equals(Items.PISTON)) {
				BlockPos pistonPos = hitResult.getBlockPos().offset(hitResult.getSide());
				try {
					BlockPos bedrockPos = pistonPos.offset(world.getBlockState(pistonPos).get(FacingBlock.FACING));
					if (world.getBlockState(bedrockPos).getBlock().equals(Blocks.BEDROCK)) {
						newPistonCaches();
						newPistonPosCaches();
						pistonPosCaches.put(bedrockPos, player);
						pistonCaches.put(world, pistonPosCaches);
						System.out.println("PlacedPistonPointToBedrockAt: " + pistonPos.toString());
					}
				} catch (IllegalArgumentException e) {

				}
			}
			return ActionResult.SUCCESS;
		});
		PistonBreakBedrockCallback.EVENT.register((world, pos) -> {
			String player = pistonCaches.get(world).get(pos).getEntityName();
			scoreboardBBL.forEach(objective -> {
				Scoreboard scoreboard = world.getScoreboard();
				try {
					scoreboard.getPlayerScore(player, objective).incrementScore();
				} catch (NullPointerException e) {

				}
			});
			System.out.println("BreakBedrockSucceeded: " + player);
			return ActionResult.PASS;
		});
		ServerTickEvents.END_WORLD_TICK.register(server -> {
			if (pistonCaches != null) {
				nullPistonCaches();
			}
		});
	}

	private static void nullPistonCaches() {
		pistonCaches = null;
	}

	private static void newPistonCaches() {
		pistonCaches = new HashMap<>();
	}

	private static void newPistonPosCaches() {
		pistonPosCaches = new HashMap<>();
	}
}