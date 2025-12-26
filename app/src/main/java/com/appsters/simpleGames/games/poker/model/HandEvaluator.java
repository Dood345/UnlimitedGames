package com.appsters.simpleGames.games.poker.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluates Texas Hold'em hands by enumerating all 5-card combinations from 7 cards.
 * Returns a comparable long where larger is better.
 *
 * Encoding:
 * - category (0..8) in the top bits (straight flush highest).
 * - then kickers / tiebreak ranks.
 */
public class HandEvaluator {

    public static class Result {
        public final long value;
        public final String categoryName;

        public Result(long value, String categoryName) {
            this.value = value;
            this.categoryName = categoryName;
        }
    }

    // Category ordering (higher is better)
    private static final int HIGH_CARD = 0;
    private static final int ONE_PAIR = 1;
    private static final int TWO_PAIR = 2;
    private static final int THREE_KIND = 3;
    private static final int STRAIGHT = 4;
    private static final int FLUSH = 5;
    private static final int FULL_HOUSE = 6;
    private static final int FOUR_KIND = 7;
    private static final int STRAIGHT_FLUSH = 8;

    public static Result bestOfSeven(List<Card> seven) {
        if (seven.size() != 7) throw new IllegalArgumentException("Need 7 cards");
        long best = Long.MIN_VALUE;
        String bestName = "High Card";

        int[] idx = new int[]{0,1,2,3,4};
        // enumerate combinations of 5 from 7 (21 combos)
        for (int a=0;a<3;a++){
            for (int b=a+1;b<4;b++){
                for (int c=b+1;c<5;c++){
                    for (int d=c+1;d<6;d++){
                        for (int e=d+1;e<7;e++){
                            List<Card> five = Arrays.asList(seven.get(a), seven.get(b), seven.get(c), seven.get(d), seven.get(e));
                            Result r = evaluateFive(five);
                            if (r.value > best) {
                                best = r.value;
                                bestName = r.categoryName;
                            }
                        }
                    }
                }
            }
        }
        return new Result(best, bestName);
    }

    public static Result evaluateFive(List<Card> five) {
        if (five.size() != 5) throw new IllegalArgumentException("Need 5 cards");

        // sort by rank desc
        List<Card> cards = new ArrayList<>(five);
        Collections.sort(cards, (c1, c2) -> Integer.compare(c2.rank, c1.rank));

        boolean flush = isFlush(cards);
        int straightHigh = straightHighRank(cards); // 0 if not straight
        boolean straight = straightHigh != 0;

        Map<Integer, Integer> counts = rankCounts(cards);
        List<Integer> uniqueRanksDesc = new ArrayList<>(counts.keySet());
        uniqueRanksDesc.sort(Comparator.reverseOrder());

        // build list of (count, rank) sorted
        List<int[]> groups = new ArrayList<>();
        for (int r : counts.keySet()) groups.add(new int[]{counts.get(r), r});
        groups.sort((g1, g2) -> {
            if (g1[0] != g2[0]) return Integer.compare(g2[0], g1[0]); // count desc
            return Integer.compare(g2[1], g1[1]); // rank desc
        });

        if (straight && flush) {
            return new Result(pack(STRAIGHT_FLUSH, new int[]{straightHigh}), "Straight Flush");
        }

        if (groups.get(0)[0] == 4) {
            int quadRank = groups.get(0)[1];
            int kicker = highestExcluding(cards, quadRank);
            return new Result(pack(FOUR_KIND, new int[]{quadRank, kicker}), "Four of a Kind");
        }

        if (groups.get(0)[0] == 3 && groups.size() > 1 && groups.get(1)[0] == 2) {
            int trips = groups.get(0)[1];
            int pair = groups.get(1)[1];
            return new Result(pack(FULL_HOUSE, new int[]{trips, pair}), "Full House");
        }

        if (flush) {
            int[] ranks = ranksDesc(cards);
            return new Result(pack(FLUSH, ranks), "Flush");
        }

        if (straight) {
            return new Result(pack(STRAIGHT, new int[]{straightHigh}), "Straight");
        }

        if (groups.get(0)[0] == 3) {
            int trips = groups.get(0)[1];
            int[] kickers = topKickersExcluding(cards, new int[]{trips}, 2);
            return new Result(pack(THREE_KIND, new int[]{trips, kickers[0], kickers[1]}), "Three of a Kind");
        }

        if (groups.get(0)[0] == 2 && groups.size() > 1 && groups.get(1)[0] == 2) {
            int highPair = Math.max(groups.get(0)[1], groups.get(1)[1]);
            int lowPair = Math.min(groups.get(0)[1], groups.get(1)[1]);
            int kicker = highestExcluding(cards, highPair, lowPair);
            return new Result(pack(TWO_PAIR, new int[]{highPair, lowPair, kicker}), "Two Pair");
        }

        if (groups.get(0)[0] == 2) {
            int pair = groups.get(0)[1];
            int[] kickers = topKickersExcluding(cards, new int[]{pair}, 3);
            return new Result(pack(ONE_PAIR, new int[]{pair, kickers[0], kickers[1], kickers[2]}), "One Pair");
        }

        int[] ranks = ranksDesc(cards);
        return new Result(pack(HIGH_CARD, ranks), "High Card");
    }

    private static boolean isFlush(List<Card> cards) {
        Card.Suit s = cards.get(0).suit;
        for (int i=1;i<cards.size();i++){
            if (cards.get(i).suit != s) return false;
        }
        return true;
    }

    /**
     * Returns the high rank of the straight (A2345 returns 5). Returns 0 if not straight.
     */
    private static int straightHighRank(List<Card> cards) {
        // unique ranks
        List<Integer> ranks = new ArrayList<>();
        for (Card c : cards) if (!ranks.contains(c.rank)) ranks.add(c.rank);
        ranks.sort(Comparator.reverseOrder());

        // Handle wheel A-2-3-4-5
        if (ranks.contains(14) && ranks.contains(5) && ranks.contains(4) && ranks.contains(3) && ranks.contains(2)) {
            return 5;
        }

        if (ranks.size() < 5) return 0;
        for (int i=0;i<4;i++){
            if (ranks.get(i) - 1 != ranks.get(i+1)) return 0;
        }
        return ranks.get(0);
    }

    private static Map<Integer,Integer> rankCounts(List<Card> cards){
        Map<Integer,Integer> m = new HashMap<>();
        for (Card c: cards) {
            Integer v = m.get(c.rank);
            m.put(c.rank, v==null?1:v+1);
        }
        return m;
    }

    private static int[] ranksDesc(List<Card> cards){
        int[] r = new int[cards.size()];
        for (int i=0;i<cards.size();i++) r[i]=cards.get(i).rank;
        return r;
    }

    private static int highestExcluding(List<Card> cards, int... exclude) {
        for (Card c : cards) {
            boolean ok = true;
            for (int ex : exclude) if (c.rank == ex) { ok=false; break; }
            if (ok) return c.rank;
        }
        return 0;
    }

    private static int[] topKickersExcluding(List<Card> cards, int[] exclude, int count) {
        int[] res = new int[count];
        int idx=0;
        for (Card c : cards) {
            boolean ok=true;
            for (int ex: exclude) if (c.rank==ex) { ok=false; break; }
            if (ok) {
                res[idx++]=c.rank;
                if (idx==count) break;
            }
        }
        return res;
    }

    /**
     * Packs a category and tiebreak ranks into a comparable long.
     * Category dominates, then ranks in order.
     */
    private static long pack(int category, int[] ranks) {
        long v = ((long)category) << 28;
        // use 4 bits per rank (2..14 fits) across up to 5 ranks
        int shift = 24;
        for (int i=0;i<Math.min(5, ranks.length);i++){
            v |= ((long)ranks[i] & 0xF) << shift;
            shift -= 4;
        }
        return v;
    }
}
