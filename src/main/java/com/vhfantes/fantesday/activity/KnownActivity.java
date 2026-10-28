package com.vhfantes.fantesday.activity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.vhfantes.fantesday.text.TextNormalizer;

// Atividades conhecidas, com o MET (gasto em relacao ao repouso) do Compendium of Physical Activities
// e as palavras usadas para reconhece-las no texto ("joguei volei", "corri 5 km", "fui pedalar"...)
public enum KnownActivity {
    CAMINHADA("Caminhada", "3.5", "caminhada", "caminhar", "caminhei", "andar", "andei", "passeio a pe", "walking"),
    CAMINHADA_RAPIDA("Caminhada rápida", "5.0", "caminhada rapida", "caminhada acelerada", "andei rapido"),
    TROTE("Trote", "7.0", "trote", "trotei", "jogging"),
    CORRIDA("Corrida", "9.8", "corrida", "correr", "corri", "running", "cooper"),
    CICLISMO("Ciclismo", "7.5", "ciclismo", "bicicleta", "bike", "pedal", "pedalar", "pedalei", "pedalada"),
    BICICLETA_ERGOMETRICA("Bicicleta ergométrica", "6.8", "bicicleta ergometrica", "bike ergometrica", "ergometrica"),
    SPINNING("Spinning", "8.5", "spinning", "bike indoor"),
    NATACAO("Natação", "7.0", "natacao", "nadar", "nadei", "swimming"),
    HIDROGINASTICA("Hidroginástica", "5.3", "hidroginastica", "hidro"),
    MUSCULACAO("Musculação", "5.0", "musculacao", "academia", "malhar", "malhei", "treino de forca", "levantamento de peso"),
    CROSSFIT("Crossfit", "8.0", "crossfit", "cross training"),
    FUNCIONAL("Treino funcional", "6.0", "funcional", "treino funcional"),
    HIIT("HIIT", "8.0", "hiit", "tabata", "treino intervalado"),
    FUTEBOL("Futebol", "7.0", "futebol", "bola", "pelada", "soccer"),
    FUTSAL("Futsal", "8.0", "futsal", "futebol de salao"),
    FUTEVOLEI("Futevôlei", "7.0", "futevolei"),
    VOLEI("Vôlei", "4.0", "volei", "voleibol", "volleyball"),
    VOLEI_DE_PRAIA("Vôlei de praia", "8.0", "volei de praia", "beach volley"),
    BASQUETE("Basquete", "6.5", "basquete", "basquetebol", "basketball"),
    HANDEBOL("Handebol", "8.0", "handebol", "handball"),
    TENIS("Tênis", "7.3", "tenis", "tennis"),
    BEACH_TENNIS("Beach tennis", "6.0", "beach tennis", "beach tenis"),
    PADEL("Padel", "6.0", "padel"),
    TENIS_DE_MESA("Tênis de mesa", "4.0", "tenis de mesa", "ping pong", "pingue pongue"),
    ARTES_MARCIAIS("Artes marciais", "10.3", "jiu jitsu", "jiujitsu", "judo", "karate", "muay thai", "boxe", "kickboxing",
            "taekwondo", "capoeira", "luta", "mma"),
    YOGA("Yoga", "2.5", "yoga", "ioga"),
    PILATES("Pilates", "3.0", "pilates"),
    ALONGAMENTO("Alongamento", "2.3", "alongamento", "alongar", "alonguei"),
    DANCA("Dança", "5.0", "danca", "dancar", "dancei", "ballet", "bale", "forro", "samba"),
    ZUMBA("Zumba", "6.5", "zumba", "aerobica"),
    PULAR_CORDA("Pular corda", "11.8", "pular corda", "pulei corda"),
    SUBIR_ESCADAS("Subir escadas", "8.0", "subir escada", "subir escadas", "subi escada", "subi escadas"),
    ELIPTICO("Elíptico", "5.0", "eliptico", "transport"),
    REMO("Remo", "7.0", "remo", "remar", "remei", "canoagem", "caiaque"),
    TRILHA("Trilha", "6.0", "trilha", "hiking", "montanhismo"),
    ESCALADA("Escalada", "7.5", "escalada", "escalar", "escalei", "boulder"),
    SKATE("Skate", "5.0", "skate"),
    PATINACAO("Patinação", "7.0", "patins", "patinacao", "patinar", "patinei"),
    SURFE("Surfe", "3.0", "surfe", "surf", "surfar", "surfei"),
    STAND_UP_PADDLE("Stand up paddle", "6.0", "stand up paddle", "stand up", "sup"),
    TAREFAS_DOMESTICAS("Tarefas domésticas", "3.3", "faxina", "limpar a casa", "limpei a casa", "tarefas domesticas"),
    JARDINAGEM("Jardinagem", "3.8", "jardinagem", "jardim");

    private final String label;
    private final BigDecimal met;
    private final List<String> aliases;

    KnownActivity(String label, String met, String... aliases) {
        this.label = label;
        this.met = new BigDecimal(met);
        // O proprio nome tambem serve: "VOLEI_DE_PRAIA" e "Vôlei de praia" sao reconhecidos
        this.aliases = Stream.concat(Stream.of(name(), label), Stream.of(aliases))
                .map(TextNormalizer::normalize)
                .distinct()
                .toList();
    }

    public String label() {
        return label;
    }

    public BigDecimal met() {
        return met;
    }

    // Procura a atividade no texto. Se varias baterem, vale a palavra mais longa:
    // "volei de praia" ganha de "volei" e "andei de bicicleta" vira ciclismo, nao caminhada.
    public static Optional<KnownActivity> find(String text) {
        String normalized = " " + TextNormalizer.normalize(text) + " ";

        KnownActivity best = null;
        int bestLength = 0;
        for (KnownActivity activity : values()) {
            for (String alias : activity.aliases) {
                if (alias.length() > bestLength && normalized.contains(" " + alias + " ")) {
                    best = activity;
                    bestLength = alias.length();
                }
            }
        }
        return Optional.ofNullable(best);
    }
}
