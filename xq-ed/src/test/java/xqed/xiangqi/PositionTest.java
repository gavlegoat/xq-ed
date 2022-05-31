package xqed.xiangqi;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.util.ArrayList;
import javafx.util.Pair;
import org.junit.jupiter.api.Test;

class PositionTest {

	@Test
	void testPositionString() {
		
		Position pos = null;
		try {
			pos = new Position("9/9/9/9/9/9/9/9/9/9");
		} catch (ParseException e) {
			fail("Unable to parse position");
		}
		for (int f = 0; f < 9; f++) {
			for (int r = 0; r < 10; r++) {
				assertFalse(pos.hasPieceAt(f, r));
			}
		}
		
		assertThrows(ParseException.class, () -> new Position("9/9/9/"));
		
		try {
			pos = new Position(
					"RHEAKAEHR/9/1C5C1/P1P1P1P1P/9/9/p1p1p1p1p/1c5c1/9/rheakaehr");
		} catch (ParseException e) {
			fail("Unable to parse position");
		}
		Position start = new Position();
		for (int f = 0; f < 9; f++) {
			for (int r = 0; r < 10; r++) {
				if (start.hasPieceAt(f, r)) {
					assertEquals(pos.pieceAt(f, r), start.pieceAt(f, r));
				}
			}
		}
		
	}

	@Test
	void testInCheck() {
		
		Position pos = null;
		try {
			pos = new Position("4K4/9/9/9/9/9/9/9/9/5k3");
		} catch (ParseException e) {
			fail("Unable to parse position");
		}
		assertTrue(pos.inCheck(Piece.Color.RED, 5, 9, 4, 9));
		assertTrue(pos.inCheck(Piece.Color.BLACK, 4, 0, 5, 0));
		assertFalse(pos.inCheck(Piece.Color.RED, 5, 9, 5, 8));
		assertFalse(pos.inCheck(Piece.Color.BLACK, 4, 0, 3, 0));
		
		try {
			pos = new Position("4K4/9/4p4/7h1/9/9/R8/9/9/4k4");
		} catch (ParseException e) {
			fail("Unable to parse position");
		}
		assertTrue(pos.inCheck(Piece.Color.RED, 4, 2, 3, 2));
		assertTrue(pos.inCheck(Piece.Color.RED, 0, 6, 0, 9));
		assertTrue(pos.inCheck(Piece.Color.BLACK, 7, 3, 6, 1));
		assertFalse(pos.inCheck(Piece.Color.RED, 4, 2, 4, 1));
		assertFalse(pos.inCheck(Piece.Color.BLACK, 0, 6, 0, 9));
	}

	@Test
	void testGetMovesFromIntIntBoolean() {
		// Many cases are also covered by other tests which rely on the inCheck
		// function.
		Position pos = new Position();
		ArrayList<Pair<Integer, Integer>> moves = pos.getMovesFrom(3,  0, true);
		assertEquals(moves.size(), 1);
		assertEquals(moves.get(0), new Pair<>(4, 1));
	}

	@Test
	void testInterpretMove() {
		Position pos = new Position();
		
		assertThrows(ParseException.class,
				() -> pos.interpretMove("P4+1", Piece.Color.RED));
		assertThrows(ParseException.class,
				() -> pos.interpretMove("k1a1", Piece.Color.RED));
		assertThrows(ParseException.class,
				() -> pos.interpretMove("", Piece.Color.RED));
		
		Move centerCannon = new Move(new Piece(Piece.Color.RED, Piece.Type.CANNON),
				new Pair<>(7, 7), new Pair<>(4, 7));
		Move m = null;
		try {
			m = pos.interpretMove("C2=5", Piece.Color.RED);
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		assertEquals(m, centerCannon);
		try {
			m = pos.interpretMove("Che3", Piece.Color.RED);
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		assertEquals(m, centerCannon);
		try {
			m = pos.interpretMove("h3e3", Piece.Color.RED);
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		assertEquals(m, centerCannon);
		
		try {
			m = pos.interpretMove("i10i9", Piece.Color.BLACK);
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		assertEquals(m, new Move(new Piece(Piece.Color.BLACK, Piece.Type.ROOK),
				new Pair<>(8, 0), new Pair<>(8, 1)));
		
		Position newPos = pos.makeMove(new Move(new Piece(Piece.Color.BLACK, Piece.Type.HORSE),
				new Pair<>(7, 0), new Pair<>(6, 2)));
		try {
			m = newPos.interpretMove("i10h10", Piece.Color.BLACK);
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		assertEquals(m, new Move(new Piece(Piece.Color.BLACK, Piece.Type.ROOK),
				new Pair<>(8, 0), new Pair<>(7, 0)));
		
		try {
			newPos = new Position("4K4/9/r8/9/9/r8/9/9/9/5k3");
		} catch (ParseException e) {
			fail("Unable to parse position");
		}
		Move rookTandem = new Move(new Piece(Piece.Color.RED, Piece.Type.ROOK),
				new Pair<>(0, 2), new Pair<>(0, 3));
		try {
			m = newPos.interpretMove("R8a7", Piece.Color.RED);
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		assertEquals(m, rookTandem);
		
		try {
			m = newPos.interpretMove("+R-1", Piece.Color.RED);
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		assertEquals(m, rookTandem);
		
		try {
			m = newPos.interpretMove("R-+1", Piece.Color.RED);
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		assertEquals(m, new Move(new Piece(Piece.Color.RED, Piece.Type.ROOK),
				new Pair<>(0, 5), new Pair<>(0, 4)));
		
		try {
			newPos = new Position("4KA3/1H7/9/P8/9/P7c/9/P1h6/9/2e2k3");
		} catch (ParseException e) {
			fail("Unable to parse position");
		}
		
		// Pawns in tandem
		try {
			m = newPos.interpretMove("31+1", Piece.Color.BLACK);
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		assertEquals(m, new Move(new Piece(Piece.Color.BLACK, Piece.Type.PAWN),
				new Pair<>(0, 3), new Pair<>(0, 4)));
		
		// Elephant
		try {
			m = newPos.interpretMove("E7+5", Piece.Color.RED);
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		assertEquals(m, new Move(new Piece(Piece.Color.RED, Piece.Type.ELEPHANT),
				new Pair<>(2, 9), new Pair<>(4, 7)));
		
		// Advisor
		try {
			m = newPos.interpretMove("A6+5", Piece.Color.BLACK);
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		assertEquals(m, new Move(new Piece(Piece.Color.BLACK, Piece.Type.ADVISOR),
				new Pair<>(5, 0), new Pair<>(4, 1)));
		
		// King
		try {
			m = newPos.interpretMove("K4+1", Piece.Color.RED);
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		assertEquals(m, new Move(new Piece(Piece.Color.RED, Piece.Type.KING),
				new Pair<>(5, 9), new Pair<>(5, 8)));
		
		// Horse
		try {
			m = newPos.interpretMove("H2+3", Piece.Color.BLACK);
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		assertEquals(m, new Move(new Piece(Piece.Color.BLACK, Piece.Type.HORSE),
				new Pair<>(1, 1), new Pair<>(2, 3)));
		
	}

}
