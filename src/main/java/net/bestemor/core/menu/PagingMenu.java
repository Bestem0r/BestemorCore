package net.bestemor.core.menu;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class PagingMenu extends Menu {

    private final PagingContent pagingContent;
    private final MenuListener listener;

    private final List<Menu> menus = new ArrayList<>();

    private boolean isCreated = false;

    protected PagingMenu(MenuListener listener, int size, String name) {
        super(listener, size, name);

        this.listener = listener;
        this.pagingContent = new PagingContent();
    }

    /** Runs when the page is initially created
     * @param content Persistent content
     * @param page Menu page */
    protected abstract void onCreatePage(MenuContent content, int page);

    /** Runs when the page is updated
     * @param content Persistent content
     * @param page Menu page */
    protected abstract void onUpdatePage(MenuContent content, int page);

    /** Runs when the PagingMenu is created or updated. PagingContent is loaded
     * and distributed across dynamically created menu pages
     * @param content Container of Clickables to distribute */
    protected abstract void onUpdatePagingContent(PagingContent content);


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
            isCreated = true;
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
            menus.add(new Menu(listener, super.getInventory().getSize(), super.getInventory().getName()) {

                @Override
                protected void onCreate(MenuContent content) {
                    onCreatePage(content, finalPage);
                }

                @Override
                protected void onUpdate(MenuContent content) {
                    onUpdatePage(content, finalPage);

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
