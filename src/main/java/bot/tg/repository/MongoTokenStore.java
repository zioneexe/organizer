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
    public String load(String id) throws IOException {
        Document doc = tokens.find(new Document("_id", id)).first();
        if (doc == null) return null;
        return doc.getString("tokens");
    }

    @Override
    public void store(String id, String tokens)  {
        Document doc = new Document("_id", id).append("tokens", tokens);
        this.tokens.replaceOne(new Document("_id", id), doc, new ReplaceOptions().upsert(true));
    }

    @Override
    public void delete(String id) {
        tokens.deleteOne(new Document("_id", id));
    }
}
