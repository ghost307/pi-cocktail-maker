package net.alex9849.cocktailmaker.repository;

import net.alex9849.cocktailmaker.model.Collection;
import org.postgresql.PGConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class CollectionRepository extends JdbcDaoSupport {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @PostConstruct
    private void initialize() {
        setDataSource(dataSource);
    }

    public Collection create(Collection collection) {
        return getJdbcTemplate().execute((ConnectionCallback<Collection>) con -> {
            PreparedStatement pstmt = con.prepareStatement("INSERT INTO collections (name, description, completed, owner_id) " +
                    "VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, collection.getName());
            pstmt.setString(2, collection.getDescription());
            pstmt.setBoolean(3, collection.isCompleted());
            pstmt.setLong(4, collection.getOwner().getId());
            pstmt.execute();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                collection.setId(rs.getLong(1));
                return collection;
            }
            throw new IllegalStateException("Error saving collection");
        });
    }

    public boolean update(Collection collection) {
        return getJdbcTemplate().execute((ConnectionCallback<Boolean>) con -> {
            PreparedStatement pstmt = con.prepareStatement("UPDATE collections SET name = ?, description = ?, " +
                    "completed = ?, owner_id = ?, last_update = CURRENT_TIMESTAMP WHERE id = ?");
            pstmt.setString(1, collection.getName());
            pstmt.setString(2, collection.getDescription());
            pstmt.setBoolean(3, collection.isCompleted());
            pstmt.setLong(4, collection.getOwner().getId());
            pstmt.setLong(5, collection.getId());
            return pstmt.executeUpdate() != 0;
        });
    }

    public boolean addRecipe(long collectionId, long recipeId) {
        return getJdbcTemplate().execute((ConnectionCallback<Boolean>) con -> {
            PreparedStatement pstmt = con.prepareStatement("INSERT INTO collection_recipes (recipe_id, collection_id) " +
                    "VALUES (?, ?)");
            pstmt.setLong(1, recipeId);
            pstmt.setLong(2, collectionId);
            return pstmt.executeUpdate() != 0;
        });
    }

    public boolean removeRecipe(long collectionId, long recipeId) {
        return getJdbcTemplate().execute((ConnectionCallback<Boolean>) con -> {
            PreparedStatement pstmt = con.prepareStatement("DELETE FROM collection_recipes WHERE " +
                    "recipe_id = ? AND collection_id = ?");
            pstmt.setLong(1, recipeId);
            pstmt.setLong(2, collectionId);
            return pstmt.executeUpdate() != 0;
        });
    }

    public boolean delete(long id) {
        return getJdbcTemplate().execute((ConnectionCallback<Boolean>) con -> {
            PreparedStatement pstmt = con.prepareStatement("DELETE FROM collections WHERE id = ?");
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() != 0;
        });
    }

    public Set<Long> findIdsOwnedByUser(long userId) {
        return getJdbcTemplate().execute((ConnectionCallback<Set<Long>>) con -> {
            PreparedStatement pstmt = con.prepareStatement("SELECT id as id FROM collections " +
                    "WHERE owner_id = ?");
            pstmt.setLong(1, userId);
            return DbUtils.executeGetIdsPstmt(pstmt);
        });
    }

    public List<Collection> findByIds(Long... ids) {
        if(ids.length == 0) {
            return new ArrayList<>();
        }
        return getJdbcTemplate().execute((ConnectionCallback<List<Collection>>) con -> {
            PreparedStatement pstmt = con.prepareStatement("SELECT * FROM collections c " +
                    "WHERE c.id = ANY(?) order by c.name");

            pstmt.setArray(1, con.createArrayOf("int8", ids));
            ResultSet rs = pstmt.executeQuery();
            List<Collection> results = new ArrayList<>();
            while (rs.next()) {
                results.add(parseRs(rs));
            }
            return results;
        });
    }

    public void setImage(long collectionId, byte[] image) {
        getJdbcTemplate().execute((ConnectionCallback<Void>) con -> {
            PreparedStatement pstmt = con.prepareStatement("SELECT image FROM collections where id = ? AND image IS NOT NULL");
            pstmt.setLong(1, collectionId);
            ResultSet resultSet = pstmt.executeQuery();
            LargeObjectManager lobApi = con.unwrap(PGConnection.class).getLargeObjectAPI();
            Long imageOid;
            if(resultSet.next()) {
                imageOid = resultSet.getObject("image", Long.class);
            } else {
                imageOid = lobApi.createLO(LargeObjectManager.READWRITE);
            }
            if(image == null) {
                PreparedStatement deleteImagePstmt = con.prepareStatement("UPDATE collections SET image = NULL, last_update = CURRENT_TIMESTAMP where id = ?");
                deleteImagePstmt.setLong(1, collectionId);
                deleteImagePstmt.executeUpdate();
                lobApi.delete(imageOid);
                return null;
            }
            LargeObject lobObject = lobApi.open(imageOid, LargeObjectManager.READWRITE);
            lobObject.write(image);
            lobObject.truncate(image.length);
            PreparedStatement updateLobOidPstmt = con.prepareStatement("UPDATE collections SET image = ?, last_update = CURRENT_TIMESTAMP where id = ?");
            updateLobOidPstmt.setLong(1, imageOid);
            updateLobOidPstmt.setLong(2, collectionId);
            updateLobOidPstmt.executeUpdate();
            return null;
        });
    }

    public Optional<byte[]> getImage(long collectionId) {
        return getJdbcTemplate().execute((ConnectionCallback<Optional<byte[]>>) con -> {
            PreparedStatement pstmt = con.prepareStatement("SELECT image FROM collections where id = ?");
            pstmt.setLong(1, collectionId);
            ResultSet resultSet = pstmt.executeQuery();
            if(resultSet.next()) {
                Long imageOid = resultSet.getObject("image", Long.class);
                if(imageOid == null) {
                    return Optional.empty();
                }
                LargeObjectManager lobApi = con.unwrap(PGConnection.class).getLargeObjectAPI();
                LargeObject imageLob = lobApi.open(imageOid, LargeObjectManager.READ);
                byte buf[] = new byte[imageLob.size()];
                imageLob.read(buf, 0 , buf.length);
                return Optional.of(buf);
            }
            return Optional.empty();
        });
    }

    private Collection populateEntity(Collection collection) {
        collection.setSize(recipeRepository.findIdsInCollection(collection.getId()).size());
        return collection;
    }

    private Collection parseRs(ResultSet rs) throws SQLException {
        Collection collection = new Collection();
        collection.setId(rs.getLong("id"));
        collection.setName(rs.getString("name"));
        collection.setDescription(rs.getString("description"));
        collection.setCompleted(rs.getBoolean("completed"));
        collection.setOwnerId(rs.getLong("owner_id"));
        collection.setHasImage(rs.getObject("image") != null);
        collection.setLastUpdate(rs.getTimestamp("last_update"));
        return populateEntity(collection);
    }
}
