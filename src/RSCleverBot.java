import org.powerbot.script.*;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Player;
import org.powerbot.script.rt6.PlayerQuery;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by EgorDm on 3/4/2016.
 */
@Script.Manifest(name = "RSCleverBot", description = "Uses Cleverbot api to chat with players automatically.")
public class RSCleverBot extends PollingScript<ClientContext> implements MessageListener {

    private static final double NORMAL_CONVERSATION_RANGE = 100D;
    private static final double CRITICAL_CONVERSATION_RANGE = 2F;
    private static final int MIN_NAME_ACCURACY = 6;
    private static final String[] OPENING_PHRASES = new String[]{"hello", "hi", "yo", "hey", "whatsup"};
    private static ChatterBot chatterBot;

    private Map<String, ChatterBotSession> companions;
    private String name;
    private boolean paused = false;


    @Override
    public void start() {
        super.start();
        paused = false;
        companions = new HashMap<>();
        name = ctx.players.local().name().toLowerCase();
        ChatterBotFactory factory = new ChatterBotFactory();
        try {
            chatterBot = factory.create(ChatterBotType.CLEVERBOT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.print("Welcome");
    }


    @Override
    public void resume() {
        super.resume();
        paused = false;
    }

    @Override
    public void suspend() {
        super.suspend();
        paused = true;
    }

    @Override
    public void stop() {
        super.stop();
        companions.clear();
        System.out.print("Goodbye");
    }


    @Override
    public void poll() {
    }

    @Override
    public void messaged(MessageEvent messageEvent) {
        if (paused ||
                messageEvent.source() == null ||
                messageEvent.source().isEmpty() ||
                messageEvent.source().equalsIgnoreCase(name) ||
                messageEvent.source().contains("wishes to duel with you")) return;
        if(checkIsMyBusiness(messageEvent)) {
            if(!companions.containsKey(messageEvent.source())) {
                try {
                    System.out.print("Yo: "+ messageEvent.source() + "\n");
                    companions.put(messageEvent.source(), chatterBot.createSession());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            chatWith(messageEvent);
        }
    }

    public boolean checkIsMyBusiness(MessageEvent event) {
        if (companions.containsKey(event.source())) {
            return true;
        }
        if(companions.containsKey(event.source())) return true;
        PlayerQuery<Player> query = ctx.players.select().within(NORMAL_CONVERSATION_RANGE);
        if(query.size() <= 2) {
            System.out.print("Size is  " + query.size() + "\n");
            return true;
        }
        for (String word : event.text().split(" ")) {
            if (word.length() >= MIN_NAME_ACCURACY && name.contains(word.toLowerCase())) {
                System.out.print("Word is  " + word + "\n");
                return true;
            }
        }
        query = query.name(Pattern.compile(String.format("(?i)(%1$s)", event.text().replace(' ', '|'))));
        if(query.size() == 0) {
            for(String phrase : OPENING_PHRASES) {
                if(event.text().toLowerCase().contains(phrase)) {
                    System.out.print("Word is  " + phrase + "\n");
                    return true;
                }
            }
        }

        System.out.print("NMB: " + event.text() + "\n");
        return false;
    }
    public void chatWith(MessageEvent event) {
        try {
            String response = companions.get(event.source()).think(event.text());
            if(ctx.widgets.widget(137).component(130).text().equals("[Press Enter to Chat]")) {
                ctx.input.send("{VK_ENTER}");
                Condition.sleep(Random.getDelay());
            }
            ctx.input.sendln(response.replace('?', ' '));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
