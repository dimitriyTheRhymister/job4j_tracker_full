package ru.job4j.tracker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HbmTrackerIntegrationTest {

    private HbmTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new HbmTracker();
    }

    @AfterEach
    void tearDown() {
        if (tracker != null) {
            tracker.close();
        }
    }

    @Test
    void whenAddItemThenItIsSaved() {
        // given
        Item item = new Item("Integration Test Item");

        // when
        Item saved = tracker.add(item);
        Item found = tracker.findById(saved.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Integration Test Item");
        assertThat(found.getId()).isNotNull().isGreaterThan(0);
        assertThat(found.getCreated()).isNotNull();
    }

    @Test
    void whenFindByIdWithNonExistentIdThenReturnsNull() {
        // when
        Item found = tracker.findById(99999);

        // then
        assertThat(found).isNull();
    }

    @Test
    void whenFindAllThenReturnsAllItems() {
        // given
        tracker.add(new Item("Item 1"));
        tracker.add(new Item("Item 2"));
        tracker.add(new Item("Item 3"));

        // when
        List<Item> items = tracker.findAll();

        // then
        assertThat(items).hasSize(3);
        assertThat(items).extracting(Item::getName)
                .containsExactly("Item 1", "Item 2", "Item 3");
    }

    @Test
    void whenFindByNameThenReturnsMatchingItems() {
        // given
        tracker.add(new Item("Alpha"));
        tracker.add(new Item("Beta Task"));
        tracker.add(new Item("Gamma"));

        // when
        List<Item> results = tracker.findByName("ta"); // case-insensitive? H2 по умолчанию — нет, но LIKE работает как регистрозависимый

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Beta Task");
    }

    @Test
    void whenFindByNameWithNoMatchThenReturnsEmptyList() {
        // given
        tracker.add(new Item("Alpha"));

        // when
        List<Item> results = tracker.findByName("XYZ");

        // then
        assertThat(results).isEmpty();
    }

    @Test
    void whenReplaceExistingItemThenUpdatesName() {
        // given
        Item original = tracker.add(new Item("Old Name"));
        int id = original.getId();

        // when
        boolean updated = tracker.replace(id, new Item(id, "New Name"));
        Item result = tracker.findById(id);

        // then
        assertThat(updated).isTrue();
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Name");
    }

    @Test
    void whenReplaceNonExistentItemThenReturnsFalse() {
        // when
        boolean updated = tracker.replace(99999, new Item("Should Fail"));

        // then
        assertThat(updated).isFalse();
    }

    @Test
    void whenDeleteExistingItemThenItIsRemoved() {
        // given
        Item item = tracker.add(new Item("To Be Deleted"));
        int id = item.getId();

        // when
        tracker.delete(id);
        Item found = tracker.findById(id);

        // then
        assertThat(found).isNull();
    }

    @Test
    void whenDeleteNonExistentItemThenNoError() {
        // Убедимся, что вызов не приводит к исключению
        tracker.delete(99999); // если будет ошибка — тест упадёт сам

        // Можно дополнительно проверить, что другие операции всё ещё работают
        Item item = tracker.add(new Item("Still Works"));
        assertThat(item).isNotNull();
    }

    @Test
    void whenAddMultipleItemsThenIdsAreUniqueAndIncreasing() {
        // given
        Item item1 = tracker.add(new Item("A"));
        Item item2 = tracker.add(new Item("B"));
        Item item3 = tracker.add(new Item("C"));

        // then
        assertThat(item1.getId()).isLessThan(item2.getId());
        assertThat(item2.getId()).isLessThan(item3.getId());
    }

    @Test
    void whenItemHasCreatedFieldThenItIsSetAutomatically() {
        // given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Item item = tracker.add(new Item("With Created"));
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        // then
        assertThat(item.getCreated()).isBetween(before, after);
    }
}