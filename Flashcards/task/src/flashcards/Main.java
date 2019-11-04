package flashcards;

import java.io.*;
import java.util.*;


public class Main {

    private static LinkedHashMap<String, String> cardToDefinition;
    private static LinkedHashMap<String, String> definitionToCard;
    private static LinkedHashMap<String, Integer> cardsWithErrors;
    private static List<String> logList;

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        cardToDefinition = new LinkedHashMap<>();
        definitionToCard = new LinkedHashMap<>();
        cardsWithErrors = new LinkedHashMap<>();
        logList = new ArrayList<>();

        String toReadFileName = null;
        String toSaveFileName = null;

        for (int i = 0; i < args.length; i += 2) {
            if ("-import".equals(args[i])) {
                toReadFileName = args[i + 1];
            } else if ("-export".equals(args[i])) {
                toSaveFileName = args[i + 1];
            }
        }

        if (toReadFileName != null) {
            loadFromFile(toReadFileName);
        }

        String action;

        while (true) {
            printMenu();
            action = readLine(scanner);
            if ("exit".equals(action)) {
                exit(toSaveFileName);
                return;
            } else if ("add".equals(action)) {
                add(scanner);
            } else if ("remove".equals(action)) {
                remove(scanner);
            } else if ("import".equals(action)) {
                importCards(scanner);
            } else if ("export".equals(action)) {
                exportCards(scanner);
            } else if ("ask".equals(action)) {
                ask(scanner);
            } else if ("log".equals(action)) {
                saveLog(scanner);
            } else if ("hardest card".equals(action)) {
                hardestCard();
            } else if ("reset stats".equals(action)) {
                resetStats(scanner);
            }
            System.out.println(); // empty string at end
        }

    }

    private static void exit(String toSaveFileName) {
        print("Bye bye!");
        if (toSaveFileName != null) {
            saveToFile(toSaveFileName);
        }
    }

    private static void saveLog(Scanner scanner) {
        print("File name:");
        String fileName = readLine(scanner);
        File file = new File(fileName);
        try (PrintWriter printWriter = new PrintWriter(file)) {

            for (String line : logList) {
                printWriter.println(line);
            }

            print("The log has been saved.");

        } catch (FileNotFoundException e) {
//            print("File not found.");
        }
    }

    private static void resetStats(Scanner scanner) {
        print("Card statistics has been reset.");
        cardsWithErrors.clear();
    }

    private static void hardestCard() {

        if (cardsWithErrors.size() < 1) {
            print("There are no cards with errors.");
            return;
        }

        int maxErrors = Collections.max(cardsWithErrors.values());

        StringBuilder cardNames = new StringBuilder();
        boolean isSubsequent = false;
        int numberOfCards = 0;

        for (Map.Entry<String, Integer> entry : cardsWithErrors.entrySet()) {
            if (entry.getValue() == maxErrors) {
                numberOfCards++;
                if (isSubsequent) {
                    cardNames.append("\", \"");
                } else {
                    isSubsequent = true;
                }
                cardNames.append(entry.getKey());
            }
        }

        if (numberOfCards > 1) {
            print("The hardest cards are \"" + cardNames.toString() + "\". You have " + maxErrors + " errors answering them.");
        } else {
            print("The hardest card is \"" + cardNames.toString() + "\". You have " + maxErrors + " errors answering them.");
        }
    }

    private static void ask(Scanner scanner) {
        print("How many times to ask?");
        String line = readLine(scanner);
        int times = Integer.parseInt(line);
        String answer;
        Random random = new Random();

        for (int i = 0; i < times; i++) {

            String key = (String) cardToDefinition.keySet().toArray()[random.nextInt(cardToDefinition.size())];

            print("Print the definition of \"" + key + "\"");
            answer = readLine(scanner);
            if (cardToDefinition.get(key).equals(answer)) {
                print("Correct answer.");
            } else {
                System.out.print("Wrong answer. ");
                cardsWithErrors.merge(key, 1, Integer::sum);
                if (definitionToCard.containsKey(answer)) {
                    print("The correct one is \"" + cardToDefinition.get(key) + "\", you've just written the definition of \"" + definitionToCard.get(answer) + "\".");
                } else {
                    print("The correct one is \"" + cardToDefinition.get(key) + "\".");
                }
            }
        }

    }

    private static void exportCards(Scanner scanner) {
        print("File name:");
        String fileName = readLine(scanner);
        saveToFile(fileName);
    }

    private static void saveToFile(String fileName) {
        File file = new File(fileName);
        try (PrintWriter printWriter = new PrintWriter(file)) {

            for (Map.Entry<String, String> entry : cardToDefinition.entrySet()) {
                printWriter.println(entry.getKey());
                printWriter.println(entry.getValue());
                printWriter.println(cardsWithErrors.get(entry.getKey()));
            }

            print(cardToDefinition.size() + " cards have been saved.");

        } catch (FileNotFoundException e) {
            print("File not found.");
        }
    }

    private static void importCards(Scanner scanner) {
        print("File name:");
        String fileName = readLine(scanner);
        loadFromFile(fileName);
    }

    private static void loadFromFile(String fileName) {
        File file = new File(fileName);
        int count = 0;

        try (Scanner scnr = new Scanner(file)) {

            while (scnr.hasNextLine()) {
                String card = scnr.nextLine();
                String definition = scnr.nextLine();
                String errors = scnr.nextLine();

                String oldDefinition = cardToDefinition.get(card);

                if (oldDefinition == null) {
                    cardToDefinition.put(card, definition);
                    definitionToCard.put(definition, card);
                    cardsWithErrors.put(card, Integer.parseInt(errors));
                } else {
                    cardToDefinition.put(card, definition);
                    definitionToCard.remove(oldDefinition);
                    definitionToCard.put(definition, card);
                    cardsWithErrors.put(card, Integer.parseInt(errors));
                }
                count++;
            }

            print(count + " cards have been loaded.");
        } catch (IOException e) {
            print("File not found.");
        }
    }

    private static void remove(Scanner scanner) {
        print("The card:");

        String card = readLine(scanner);
        String definition = cardToDefinition.get(card);

        if (definition != null) {
            cardToDefinition.remove(card);
            definitionToCard.remove(definition);
            cardsWithErrors.remove(card);
            print("The card has been removed.");
        } else {
            print("Can't remove \"" + card + "\": there is no such card.");
        }
    }

    private static void add(Scanner scanner) {
        print("The card:");
        String card = readLine(scanner);

        if (cardToDefinition.containsKey(card)) {
            print("The card \"" + card + "\" already exists.");
            return;
        }

        print("The definition of the card:");
        String definition = readLine(scanner);
        if (definitionToCard.containsKey(definition)) {
            print("The definition \"" + definition + "\" already exists.");
            return;
        }
        cardToDefinition.put(card, definition);
        definitionToCard.put(definition, card);
        print("The pair (\"" + card + "\":\"" + definition + "\") has been added.");
    }

    private static void printMenu() {
        print("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
    }

    private static String readLine(Scanner scanner) {
        String line = scanner.nextLine();
        log(line);
        return line;
    }

    private static void print(String message) {
        log(message);
        System.out.println(message);
    }

    private static void log(String message) {
        logList.add(message);
    }
}
