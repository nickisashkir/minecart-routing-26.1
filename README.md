# ![minecart routing](assets/logo.svg)

Hi! I'm Nicky! I have a fabric server at https://theremnants.gg/ and I love this addon! So I updated it to use Fabric and to be on 26.1.

A Minecraft plugin for ~Spigot/Paper/Purpur~ Fabric which adds ticket based filtering for detector rails. This enables the creation of advanced minecart routing systems.

Minecart Routing is entirely server-side, clients don't need to install anything for the plugin to function.

## Usage

Minecart Routing uses 'tickets' to determine if a detector rail should be activated or not. A different ticket for each dye can be crafted by combinding it with paper in a crafting table.

![image of ticket crafting recipe](assets/recipes.gif)

Dectector rails can have a filter set by right-clicking them with a dye. This will cause them to only activate when a minecart with the corresponding ticket in it or any of its passenger's inventories passes over it.

![An example of two different players being routed different directions](assets/routing.gif)

Filtered detector rails can also be inverted, so that they only activate with any ticket OTHER than its color.

## Installation

Drop the built jar into your server's `mods/` folder. No client-side
install required — vanilla clients connect as normal.

---

## Building from source

```
./gradlew build
```

Output jar: `build/libs/minecart-routing-<version>.jar`.

## Toolchain pins

- Minecraft `26.1` (deobfuscated jar — no `client_mappings` published
  by Mojang, vanilla class names ship in the jar directly)
- Fabric Loader `0.18.5`
- Fabric API `0.144.4+26.1`
- Fabric Loom `1.17.0-alpha.5` (`net.fabricmc.fabric-loom` plugin id,
  which is the no-remap variant — the legacy `fabric-loom` id is the
  remap-required one)
- Gradle `9.4.0` (Loom 1.17-alpha requires 9.4)
- Java `25`
- Mixin compatibility level `JAVA_25` (matches Fabric API's own config)

No `mappings` dependency is declared, no `modImplementation`. This mirrors
Fabric's own 26.1 build setup.

## API verification (against the 26.1 deobf jar)

Every class and method used by the mod was checked with `javap` against
`minecraft-merged-deobf-26.1.jar`. Verified:

- `Identifier.fromNamespaceAndPath(ns, path)` at `net.minecraft.resources.Identifier`
- `AbstractMinecart` at `net.minecraft.world.entity.vehicle.minecart.AbstractMinecart`
- `Level.isClientSide()` method (shadow-private field with same name, must use method call)
- `LevelChunk.markUnsaved()` no-arg
- `Entity.entityTags()` returns `Set<String>`, `Entity.addTag(String)`, `Entity.getPassengers()` returns `List<Entity>`
- `Player.getInventory()` returns `Inventory` (at `net.minecraft.world.entity.player.Inventory`, implements `Container`)
- `Container.getContainerSize()`, `Container.getItem(int)` at `net.minecraft.world.Container`
- `ContainerEntity.getItemStacks()` returns `NonNullList<ItemStack>`
- `CompoundTag.getString(key)` returns `Optional<String>`
- `CustomData.copyTag()` returns `CompoundTag`, `DataComponents.CUSTOM_DATA` exists
- `BlockState.is(Block)` via `TypedInstance<Block>`, `BlockState.getValue(Property)` via `StateHolder`
- `ItemStack.is(Item)` via `ItemInstance → TypedInstance<Item>`
- `Display.ItemDisplay(EntityType, Level)` ctor public, `getSlot(int)` public, `SlotAccess.set(ItemStack)` public
- `Vec3.atCenterOf(Vec3i)` (BlockPos extends Vec3i), `Entity.setPos(Vec3)` final on Entity
- `Level.getEntities(EntityTypeTest, AABB, Predicate)` with `EntityType<T> implements EntityTypeTest<Entity, T>`
- `DetectorRailBlock.SHAPE`, `DetectorRailBlock.POWERED` public static fields
- `DetectorRailBlock.checkPressed(Level, BlockPos, BlockState)` private method
- `DetectorRailBlock.getInteractingMinecartOfType(Level, BlockPos, Class, Predicate)` private method on DetectorRailBlock
- Fabric API `UseBlockCallback.interact(Player, Level, InteractionHand, BlockHitResult) → InteractionResult`
- Fabric API `AttackBlockCallback.interact(Player, Level, InteractionHand, BlockPos, Direction) → InteractionResult`
- Fabric API `AttachmentRegistry.<A>builder().persistent(Codec<A>).initializer(Supplier<A>).buildAndRegister(Identifier)`
- Fabric API `AttachmentTarget.getAttached / setAttached / getAttachedOrCreate / markUnsaved` injected on `LevelChunk`

Private methods (`Display.setTransformation`, `Display.ItemDisplay.setItemStack`) are reached via an accessor mixin and `display.getSlot(0).set(stack)` respectively.

## How it works

- **Recipes.** 16 shapeless JSON recipes in `data/minecart_routing/recipe/`.
  Each combines paper + a dye to produce a vanilla `filled_map` stamped
  with a `minecraft:custom_data` tag of `{"minecart_routing:ticket": "<color>"}`.
  The stack also gets a custom item name, a colored lore line, and a
  matching map color. Vanilla clients render all of this natively.

- **Filter assignment.** `Assigner` registers `UseBlockCallback` and
  `AttackBlockCallback`:
  - right-click a detector rail with a dye → set a whitelist filter for that color
  - right-click empty-handed → invert (whitelist ↔ blacklist)
  - left-click → clear the filter
  - sneak disables all of the above so normal breaking still works

  Each change spawns or updates a tagged `Display.ItemDisplay` on the rail
  showing concrete (whitelist) or stained glass (blacklist) in the filter's
  color. The display transform matches the rail's shape (flat vs ascending).

- **Storage.** `FilterAttachment` uses the Fabric Data Attachment API to
  keep a `Map<BlockPos, DyeFilter>` attached to each `LevelChunk`. The
  attachment is persistent with a codec so it loads and saves with the
  chunk, exactly like the Paper plugin's per-chunk PDC.

- **Router / mixin.** `DetectorRailBlockMixin` intercepts the cart list
  that vanilla's `checkPressed` gathers via `getInteractingMinecartOfType`,
  and if the rail has a filter, calls `Router.anyMatches` on that list
  to look for any cart (or any of its recursive passengers) carrying a
  matching ticket in a player or minecart inventory. If nothing matches,
  the mixin hands vanilla an empty list and the rail stays unpowered —
  which is exactly the effect the original plugin achieved by zeroing
  out the `BlockRedstoneEvent` current.

- **Cleanup.** If the mixin looks up a filter and the block is no longer
  a detector rail (broken, exploded, piston-moved, whatever), the entry
  is dropped and the visual display is removed.
