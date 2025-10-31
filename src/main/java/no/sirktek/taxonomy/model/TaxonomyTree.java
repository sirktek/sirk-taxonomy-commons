package no.sirktek.taxonomy.model;

import lombok.Builder;

import java.util.List;

/**
 * Represents a complete taxonomy tree with root categories and their hierarchical children
 *
 * @param rootCategories the top-level categories in the taxonomy tree
 */
@Builder
public record TaxonomyTree(
        List<CategoryInfo> rootCategories
) {
    /**
     * Find a category by its English class name (URI fragment)
     * @param className the English class name to search for
     * @return the category info if found, null otherwise
     */
    public CategoryInfo findByClassName(String className) {
        return findCategoryRecursively(rootCategories, className);
    }


    private CategoryInfo findCategoryRecursively(List<CategoryInfo> categories, String className) {
        for (CategoryInfo category : categories) {
            if (category.className().equals(className)) {
                return category;
            }
            CategoryInfo found = findCategoryRecursively(category.children(), className);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

}
