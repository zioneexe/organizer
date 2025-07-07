package bot.tg.repository;

import com.google.auth.oauth2.TokenStore;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

public class MongoTokenStore implements TokenStore {

    private static final String COLLECTION_NAME = "tokens";

    private final MongoCollection<Document> tokens;

    public MongoTokenStore(MongoDatabase database) {
        this.tokens = database.getCollection(COLLECTION_NAME);
    }

    @Override
    public String load(String userIdString) {
        long userId = Long.parseLong(userIdString);
        Document document = tokens.find(new Document("user_id", userId)).first();
        return document != null ? document.getString("tokens") : null;
    }

    @Override
    public void store(String userIdString, String tokens)  {
        long userId = Long.parseLong(userIdString);

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
