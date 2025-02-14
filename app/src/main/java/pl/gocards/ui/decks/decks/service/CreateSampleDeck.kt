package pl.gocards.ui.decks.decks.service

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gocards.db.deck.AppDeckDbUtil
import pl.gocards.db.room.DeckDatabase
import pl.gocards.db.storage.DatabaseException
import pl.gocards.room.entity.deck.Card
import pl.gocards.room.entity.deck.Card.Companion.setHtmlFlags
import pl.gocards.room.util.TimeUtil
import pl.gocards.util.FirebaseAnalyticsHelper
import java.nio.file.Path
import java.util.LinkedList

/**
 * @author Grzegorz Ziemski
 */
class CreateSampleDeck(private var application: Application) : AndroidViewModel(application) {

    companion object {
        private const val DECK_NAME = "Sample Deck"

        private val SAMPLE_DECK = listOf(
            arrayOf(
                "Embed YouTube in portrait mode",
                "ytp:https://www.youtube.com/embed/rthjtSAq13A"
            ),
            arrayOf(
                "Embed YouTube in landscape mode",
                "yt:https://www.youtube.com/embed/rthjtSAq13A"
            ),
            arrayOf(
                "The Honest AI Assistant",
                "<pre><code>class AI {\n" +
                        " String answer(String question) {\n" +
                        "  return switch (question.toLowerCase()) {\n" +
                        "   case \"is java better than kotlin?\" ->\n" +
                        "     \"Uhh... next question!\";\n\n" +
                        "   case \"should i use recursion?\" ->\n" +
                        "     \"Only if you enjoy stack overflows!\";\n\n" +
                        "   case \"can i deploy on friday?\" ->\n" +
                        "     \"Oh no, absolutely not!\";\n" +
                        "   default -> \"I pretend I do not see it.\";\n" +
                        "  };\n" +
                        " }\n" +
                        "\n" +
                        " public static void main(String[] args) {\n" +
                        "  AI assistant = new AI();\n" +
                        "  System.out.println(\n" +
                        "    assistant.answer(\"Can I deploy on Friday?\")\n" +
                        "  );\n" +
                        " }\n" +
                        "}\n"
            ),
            arrayOf(
                "What has 13 hearts, but no other organs?",
                "A deck of cards."
            ),
            arrayOf(
                "What building has the most stories?",
                "The public library."
            ),
            arrayOf(
                "Why did the spider get a job in I.T.?",
                "He was a great web designer."
            ),
            arrayOf(
                "I follow you all the time and copy your every move, but you can’t touch me or catch me. What am I?",
                "Your shadow."
            ),
            arrayOf(
                "What is easier to get into than out of?",
                "Trouble."
            ),
            arrayOf(
                "What is always right in front of you, but you cannot see it?",
                "The future."
            ),
            arrayOf(
                "When does a British potato change its nationality?",
                "When it becomes a french fries."
            ),
            arrayOf(
                "What is blue and not very heavy?",
                "Light blue."
            ),
            arrayOf(
                "What do you call a witch that lives on the beach?",
                "Sand-witch"
            ),
            arrayOf(
                "Why did Mickey Mouse go to space?",
                "To look for Pluto!"
            ),
            arrayOf(
                "When is the moon heaviest?",
                "When it is full."
            ),
            arrayOf(
                "Why does Voldemort prefer Twitter over Facebook?",
                "He has only followers, not friends."
            ),
            arrayOf(
                "Why did Robin Hood steal from the rich?",
                "The poor didn't have anything worth stealing!"
            ),
            arrayOf(
                "Why can not Cinderella play soccer?",
                "She is always running away from the ball!"
            ),
            arrayOf(
                "Why is Peter Pan flying all the time?",
                "He Neverlands!"
            ),
            arrayOf(
                "What does the Loch Ness monster eat?",
                "Fish and Ships"
            ),
            arrayOf(
                "How does every English joke start?",
                "By looking over your shoulder."
            ),
            arrayOf(
                "How do you measure a snake?",
                "In inches — they don’t have feet."
            ),
            arrayOf(
                "Where should you go in the room if you are feeling cold?",
                "The corner — they are usually 90 degrees."
            ),
            arrayOf(
                "Why don’t blind people skydive?",
                "It scares their dogs."
            ),
            arrayOf(
                "What kind of shoes does a spy wear?",
                "Sneakers"
            ),
            arrayOf(
                "What goes up but never comes back down?",
                "Your age."
            ),
            arrayOf(
                "What does nobody want, yet nobody wants to lose?",
                "Work."
            ),
            arrayOf(
                "If I have it, I don’t share it.  If I share it, I don’t have it. What is it?",
                "A secret."
            ),
            arrayOf(
                "What has no beginning, end, or middle?",
                "A circle."
            ),
            arrayOf(
                "What type of music do rabbits like?",
                "Hip Hop!"
            ),
            arrayOf(
                "What do you call a bear with no teeth?",
                "A gummy bear!"
            ),
            arrayOf(
                "Why are spiders so smart??",
                "They can find everything on the web."
            ),
            arrayOf(
                "What do you call two suns fighting each other?",
                "Star Wars"
            ),
            arrayOf(
                "Which fruit is always sad?",
                "A blueberry."
            )
        )
    }

    @Throws(DatabaseException::class)
    fun create(
        folder: Path,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val deckDbUtil = getAppDeckDbUtil(application)
            val deckDbPath = deckDbUtil.findFreePath("$folder/$DECK_NAME.db")
            val deckDb = createDatabase(application, deckDbPath)

            val cards = LinkedList<Card>()
            val updatedAt = TimeUtil.getNowEpochSec()

            for (i in SAMPLE_DECK.indices) {
                val sampleCard = SAMPLE_DECK[i]
                val card = Card()
                card.ordinal = i + 1
                Card.setTerm(card, sampleCard[0])
                Card.setDefinition(card, sampleCard[1])
                card.createdAt = updatedAt
                card.updatedAt = updatedAt
                setHtmlFlags(card)
                cards += card
            }

            deckDb.cardKtxDao().insertAll(cards)

            onSuccess()

            FirebaseAnalyticsHelper.getInstance(application)
                .createSampleDeck()
        }
    }

    @Throws(DatabaseException::class)
    private fun createDatabase(
        application: Context,
        dbPath: String
    ): DeckDatabase {
        return getAppDeckDbUtil(application).createDatabase(application, dbPath)
    }

    private fun getAppDeckDbUtil(application: Context): AppDeckDbUtil {
        return AppDeckDbUtil.getInstance(application)
    }
}