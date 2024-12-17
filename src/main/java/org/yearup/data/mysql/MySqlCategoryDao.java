package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{
    @Autowired
    public MySqlCategoryDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Category> getAllCategories()
    {
        List<Category> categories = new ArrayList<>();

        String query = "SELECT * FROM categories ";

        try (Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Category category = mapRow(resultSet);
                    categories.add(category);
                }
        } catch (SQLException e) {
            throw new RuntimeException("ERROR: Cannot retrieve Categories", e);
        }
        return categories;
    }

    @Override
    public Category getById(int categoryId)
    {
        String query = "SELECT * FROM categories WHERE category_id = ? ";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, categoryId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("ERROR: Cannot retrieve CategoryID #" + categoryId, e);
        }
        return null;
    }

    @Override
    public Category create(Category category)
    {
        String query = "INSERT INTO categories (name, description) VALUES (?, ?)";

        try (Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, category.getName());
            preparedStatement.setString(2, category.getDescription());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();

                if (generatedKeys.next()) {
                    int categoryId = generatedKeys.getInt(1);
                    return getById(categoryId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("ERROR: Cannot insert into table.", e);
        }
        return null;
    }

    @Override
    public void update(int categoryId, Category category)
    {
        String query = """
                UPDATE categories
                SET name = ? , description = ?
                WHERE category_id = ?
                """;

        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, category.getName());
            preparedStatement.setString(2, category.getDescription());
            preparedStatement.setInt(3, categoryId);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("ERROR: Cannot update table.", e);
        }
    }

    @Override
    public void delete(int categoryId)
    {
        String query = "DELETE FROM categories WHERE category_id = ?";

        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setInt(1, categoryId);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("ERROR: Cannot delete from table.", e);
        }
    }

    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        Category category = new Category()
        {{
            setCategoryId(categoryId);
            setName(name);
            setDescription(description);
        }};

        return category;
    }

}
