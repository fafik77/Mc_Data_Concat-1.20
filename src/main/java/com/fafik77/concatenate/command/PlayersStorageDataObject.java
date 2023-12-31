package com.fafik77.concatenate.command;

import com.fafik77.concatenate.util.singletons;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Function;


public class PlayersStorageDataObject implements DataCommandObject {
	/**
	 * throws error when not a player
	 */
	private static final SimpleCommandExceptionType INVALID_ENTITY_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.data.playerstorage.invalid"));
	/**
	 * use my type of data storage 2023-12-31 started:(2023-12-11)
	 */
	private final PlayersStorageMgr storage;
	private final Entity playerEntity;

	PlayersStorageDataObject(PlayersStorageMgr storage, Entity playerEntity) {
		this.storage = storage;
		this.playerEntity = playerEntity;
	}

	public static final Function<String, DataCommand.ObjectType> TYPE_FACTORY = (argumentName) -> {
		return new DataCommand.ObjectType() {
			public DataCommandObject getObject(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
				return new PlayersStorageDataObject(PlayersStorageDataObject.of(context), EntityArgumentType.getEntity(context, argumentName));
			}

			public ArgumentBuilder<ServerCommandSource, ?> addArgumentsToBuilder(ArgumentBuilder<ServerCommandSource, ?> argument, Function<ArgumentBuilder<ServerCommandSource, ?>, ArgumentBuilder<ServerCommandSource, ?>> argumentAdder) {
				return argument.then(CommandManager.literal("storageplayer").then((ArgumentBuilder) argumentAdder.apply(CommandManager.argument(argumentName, EntityArgumentType.entity() ))));
			}
		};
	};

	/**
	 * use my singletons instead of MinecraftServer.class to contain my type of data storage 2023-12-31, concept:2023-12-11
	 */
	static PlayersStorageMgr of( @Nullable CommandContext<ServerCommandSource> context) {
		return singletons.playersStorage.playersStorageMgr;
	}

	/** can only work on player entity */
	public void setNbt(NbtCompound nbt) throws CommandSyntaxException {
		if (this.playerEntity instanceof PlayerEntity) {
			this.storage.set(this.playerEntity.getUuid(), nbt);
		} else {
			throw INVALID_ENTITY_EXCEPTION.create();
		}
	}
	/** can only work on player entity */
	public NbtCompound getNbt() throws CommandSyntaxException {
		if (this.playerEntity instanceof PlayerEntity) {
			return this.storage.get(this.playerEntity.getUuid());
		} else {
			throw INVALID_ENTITY_EXCEPTION.create();
		}
	}

	public Text feedbackModify() {
		return Text.translatable("commands.data.playerstorage.modified", new Object[]{this.playerEntity.getDisplayName()});
	}

	public Text feedbackQuery(NbtElement element) {
		return Text.translatable("commands.data.playerstorage.query", new Object[]{this.playerEntity.getDisplayName(), NbtHelper.toPrettyPrintedText(element)});
	}

	public Text feedbackGet(NbtPathArgumentType.NbtPath path, double scale, int result) {
		return Text.translatable("commands.data.playerstorage.get", new Object[]{path, this.playerEntity.getDisplayName(), String.format(Locale.ROOT, "%.2f", scale), result});
	}
}


// */