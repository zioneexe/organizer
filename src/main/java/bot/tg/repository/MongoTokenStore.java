package bot.tg.repository;

import com.google.auth.oauth2.TokenStore;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.io.IOException;

public class MongoTokenStore implements TokenStore {

    private static final String COLLECTION_NAME = "tokens";

    private final MongoCollection<Document> tokens;

    public MongoTokenStore(MongoDatabase database) {
        this.tokens = database.getCollection(COLLECTION_NAME);
    }

    @Override
    public String load(String userId) throws IOException {
        Document document = tokens.find(new Document("user_id", userId)).first();
        if (document == null) return null;
        return document.getString("tokens");
    }

    @Override
    public void store(String userId, String tokens)  {
        Document filter = new Document("user_id", userId);
        Document document = new Document("user_id", userId)
                .append("tokens", tokens);
        this.tokens.replaceOne(filter, document, new ReplaceOptions().upsert(true));
    }

    @Override
    public void delete(String id) {
        tokens.deleteOne(new Document("_id", id));
    }
}
