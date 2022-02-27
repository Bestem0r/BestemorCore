package net.bestemor.core.menu;

import java.util.ArrayList;
import java.util.List;

public class PagingContent {

    private final List<Clickable> clickables = new ArrayList<>();

    public void addClickable(Clickable clickable) {
        clickables.add(clickable);
    }

    protected List<Clickable> getClickables() {
        return clickables;
    }

    public void clear() {
        clickables.clear();
    }
}
