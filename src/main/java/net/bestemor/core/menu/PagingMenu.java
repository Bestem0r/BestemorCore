package net.bestemor.core.menu;

import net.bestemor.core.CorePlugin;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public abstract class PagingMenu extends Menu {

    private final PagingContent pagingContent;

    private final List<Menu> menus = new ArrayList<>();
    private final String name;

    private ItemStack nextItem;
    private ItemStack previousItem;
    private int nextSlot = -1;
    private int previousSlot = -1;

    private boolean isCreated = false;

    @Deprecated
    @SuppressWarnings("unused")
    protected PagingMenu(MenuListener listener, int size, String name) {
        super(size, name);

        this.name = name;
        this.pagingContent = new PagingContent();
    }

    protected PagingMenu(int size, String name) {
        super(size, name);

        this.name = name;
        this.pagingContent = new PagingContent();
    }

    protected PagingMenu(MenuConfig config) {
        super(config);

        this.name = config.getTitle();
        this.pagingContent = new PagingContent();
    }

    /** Runs when the page is initially created
     * @param content Persistent content
     * @param page Menu page */
    protected abstract void onCreatePage(MenuContent content, int page);

    /** Runs when the page is updated
     * @param content Persistent content
     * @param page Menu page */
    protected void onUpdatePage(MenuContent content, int page) {}

    /** Runs when the PagingMenu is created or updated. PagingContent is loaded
     * and distributed across dynamically created menu pages
     * @param content Container of Clickables to distribute */
    protected abstract void onUpdatePagingContent(PagingContent content);

    /** Sets ItemStack and slot used for the "next page" UI element
     * @param nextItem ItemStack used as the "next page" item
     * @param slot Inventory slot used */
    public void setNextItem(ItemStack nextItem, int slot) {
        if (slot >= getInventory().getSize()) {
            throw new IllegalArgumentException("Inventory slot cannot be greater than or equal inventory size (" + getInventory().getSize() + ")! Got " + slot);
        }
        this.nextItem = nextItem;
        this.nextSlot = slot;
    }

    /** Sets ItemStack and slot used for the "next page" UI element
     * @param clickable PlacedClickable used as the "next page" item */
    public void setNextItem(PlacedClickable clickable) {
        setNextItem(clickable.getItem(), clickable.getSlot());
    }

    /** Sets ItemStack and slot used for the "previous page" UI element
     * @param clickable PlacedClickable used as the "previous page" item */
    public void setPreviousItem(PlacedClickable clickable) {
        setPreviousItem(clickable.getItem(), clickable.getSlot());
    }

    /** Sets ItemStack and slot used for the "previous page" UI element
     * @param previousItem ItemStack used as the "previous page" item
     * @param slot Inventory slot used */
    public void setPreviousItem(ItemStack previousItem, int slot) {
        if (slot >= getInventory().getSize()) {
            throw new IllegalArgumentException("Inventory slot cannot be greater than or equal inventory size (" + getInventory().getSize() + ")! Got " + slot);
        }
        this.previousItem = previousItem;
        this.previousSlot = slot;
    }

    protected void onClick(InventoryClickEvent event, int page) {}
    protected void onClose(InventoryCloseEvent event, int page) {}

    /** Returns the inventory slots used to distribute paged content
     * @return List of inventory slots */
    protected abstract List<Integer> getPagingSlots();

    @Override
    protected void onCreate(MenuContent content) {}

    @Override
    public void create() {
        onCreate(getContent());
        createPages();
        isCreated = true;
    }

    @Override
    public void update() {
        createPages();

        menus.forEach(Menu::update);
    }

    @Override
    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int page) {
        if (!isCreated) {
            create();
        }
        menus.get(page).open(player);
    }

    public int getPages() {
        return menus.size();
    }

    @Override
    public List<HumanEntity> getViewers() {
        return menus.stream().flatMap(m -> m.getViewers().stream()).collect(Collectors.toList());
    }

    @Override
    public void close() {
        menus.forEach(Menu::close);
    }

    @Override
    public boolean hasPlayer(HumanEntity entity) {
        return menus.stream().anyMatch(m -> m.hasPlayer(entity));
    }

    private void createPages() {

        pagingContent.clear();
        onUpdatePagingContent(pagingContent);

        int pages = Math.max(1, (int) Math.ceil(pagingContent.getClickables().size() / (double) getPagingSlots().size()));

        for (int page = menus.size(); page < pages; page++) {
            int finalPage = page;
            int size = super.getInventory().getSize();
            String menuName = name.replace("%page%", String.valueOf(page + 1));
            menus.add(new Menu(size, menuName) {
                @Override
                protected void onCreate(MenuContent content) {
                    onCreatePage(content, finalPage);
                }

                @Override
                protected void onUpdate(MenuContent content) {
                    onUpdatePage(content, finalPage);

                    if (previousItem != null && finalPage != 0) {
                        content.setClickable(previousSlot, Clickable.of(previousItem, (event) -> {
                            PagingMenu.this.open((Player) event.getWhoClicked(), finalPage - 1);
                        }));
                    }
                    if (nextItem != null && finalPage < getPages() - 1) {
                        content.setClickable(nextSlot, Clickable.of(nextItem, (event) -> {
                            PagingMenu.this.open((Player) event.getWhoClicked(), finalPage + 1);
                        }));
                    } else if (content.getLastFilledItem() != null && nextSlot != -1) {
                        content.setClickable(nextSlot, Clickable.empty(content.getLastFilledItem()));
                    } else if (nextSlot != -1) {
                        content.setClickable(nextSlot, null);
                    }

                    List<Integer> slots = getPagingSlots();

                    int start = finalPage * slots.size();
                    int end = Math.min(pagingContent.getClickables().size(), (finalPage + 1) * slots.size());
                    List<Clickable> clickables = pagingContent.getClickables().subList(start, end);

                    for (int slot = 0; slot < slots.size(); slot ++) {
                        if (slot < clickables.size()) {
                            this.getContent().setClickable(slots.get(slot), clickables.get(slot));
                        } else {
                            this.getContent().setClickable(slots.get(slot), new Clickable(null));
                        }
                    }
                }

                @Override
                protected void onClick(InventoryClickEvent event) {
                    PagingMenu.this.onClick(event, finalPage);
                }

                @Override
                protected void onClose(InventoryCloseEvent event) {
                    PagingMenu.this.onClose(event, finalPage);
                }
            });
        }
        List<Menu> cached = new ArrayList<>(menus.subList(0, pages));
        menus.clear();
        menus.addAll(cached);
    }
}
