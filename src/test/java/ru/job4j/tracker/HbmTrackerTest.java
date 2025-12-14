package ru.job4j.tracker;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HbmTrackerTest {
    @Test
    public void whenAddNewItemThenTrackerHasSameItem() throws Exception {
        try (var tracker = new HbmTracker()) {
            Item item = new Item();
            item.setName("test1");
            tracker.add(item);
            Item result = tracker.findById(item.getId());
            assertThat(result.getName()).isEqualTo(item.getName());
        }
    }
}