package xqed.xiangqi;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import javafx.util.Pair;
import org.junit.jupiter.api.Test;

class MoveTest {

	@Test
	void testPointToString() {
		assertEquals(Move.pointToString(0, 0), "a10",
				"(0, 0) should represent a10");
		assertEquals(Move.pointToString(0, 9), "a1",
				"(0, 9) should represent a1");
		assertEquals(Move.pointToString(8, 0), "i10",
				"(8, 0) should represent i10");
		assertEquals(Move.pointToString(8, 9), "i1",
				"(8, 9) should represent i1");
		assertEquals(Move.pointToString(4, 4), "e6",
				"(4, 4) should represent e6");
	}

	@Test
	void testWrite() {
		
		// Use the starting position
		Position start = new Position();
		
		// Basic moves + file ambiguity
		Move centralCannon = new Move(new Piece(
				Piece.Color.RED, Piece.Type.CANNON), new Pair<>(7, 7), new Pair<>(4, 7));
		assertEquals(centralCannon.write(start, Move.MoveFormat.RELATIVE), "C2=5",
				"The central cannon opening should be written R2=5");
		assertEquals(centralCannon.write(start, Move.MoveFormat.ALGEBRAIC), "Che3",
				"The central cannon opening should be written Rhe3");
		assertEquals(centralCannon.write(start, Move.MoveFormat.UCCI), "h3e3",
				"The central cannon opening should be written h3e3");
		start = start.makeMove(centralCannon);
		Move screenHorse = new Move(new Piece(
				Piece.Color.BLACK, Piece.Type.HORSE), new Pair<>(7, 0), new Pair<>(6, 2));
		assertEquals(screenHorse.write(start, Move.MoveFormat.RELATIVE), "H8+7",
				"The screen horse should be written H8+7");
		assertEquals(screenHorse.write(start, Move.MoveFormat.ALGEBRAIC), "Hg8",
				"The screen horse should be written Hg8");
		assertEquals(screenHorse.write(start, Move.MoveFormat.UCCI), "h10g8",
				"The screen horse should be written h10g8");
		
		// For the rest we only test the move formats that need new behavior.
		// UCCI is simple and doesn't need any more testing.

		// . . . . K . . . .
		// r . . . . . . . .
		// . . . . . . . . .
		// . . . . . . . P .
		// r . . . . . . . .
		// . . . . . . . . .
		// . . . . . . . P .
		// . . . . . . . . .
		// . . . . . . . P .
		// . . . k . . . . .
		Position testPos = null;
		try {
			testPos = new Position("4K4/r6P1/9/7P1/r8/9/7P1/9/9/3k5");
		} catch (ParseException e) {
			fail("Unable to parse position");
		}
		
		// Tandem pieces
		Move frontRook = new Move(new Piece(Piece.Color.RED, Piece.Type.ROOK),
				new Pair<>(0, 1), new Pair<>(1, 1));
		assertEquals(frontRook.write(testPos, Move.MoveFormat.RELATIVE), "+R=8",
				"Tandem rooks should be indicated with + and -");
		assertEquals(frontRook.write(testPos, Move.MoveFormat.ALGEBRAIC), "Rb9",
				"Tandem rooks don't always need clarification");
		Move backRook = new Move(new Piece(Piece.Color.RED, Piece.Type.ROOK),
				new Pair<>(0, 4), new Pair<>(0, 3));
		assertEquals(backRook.write(testPos, Move.MoveFormat.RELATIVE), "-R+1",
				"Tandem rooks should be indicated with + and -");
		assertEquals(backRook.write(testPos, Move.MoveFormat.ALGEBRAIC), "R6a7",
				"Tandem pieces can be clarified by row number");
		assertEquals(new Move(new Piece(Piece.Color.RED, Piece.Type.ROOK),
				new Pair<>(0, 1), new Pair<>(0, 3)).write(testPos,
						Move.MoveFormat.RELATIVE), "+R-2");
		
		// Pawns in tandem
		Move frontPawn = new Move(new Piece(Piece.Color.BLACK, Piece.Type.PAWN),
				new Pair<>(7, 8), new Pair<>(6, 8));
		assertEquals(frontPawn.write(testPos, Move.MoveFormat.RELATIVE), "18=7",
				"Tandem pawns should be numbered");
		assertEquals(frontPawn.write(testPos, Move.MoveFormat.ALGEBRAIC), "Pg2",
				"Tandem pawns don't need to be differentiated in algebraic");
		Move centerPawn = new Move(new Piece(Piece.Color.BLACK, Piece.Type.PAWN),
				new Pair<>(7, 6), new Pair<>(7, 7));
		assertEquals(centerPawn.write(testPos, Move.MoveFormat.RELATIVE), "28+1",
				"Tandem pawns should be numbered");
		Move backPawn = new Move(new Piece(Piece.Color.BLACK, Piece.Type.PAWN),
				new Pair<>(7, 3), new Pair<>(7, 4));
		assertEquals(backPawn.write(testPos, Move.MoveFormat.RELATIVE), "38+1",
				"Tandem pawns should be numbered");
		
		// WXF doesn't distinguish these kinds of moves.
		
		// . . . . K C . . .
		// . . . . . A . . .
		// . . . . . E . . .
		// . . . . . . . . .
		// . . . . . . . p .
		// h . . . . . . . .
		// . . . . . . . . H
		// R . . . . . p . .
		// . . . . . . . . .
		// . . . . . k . . .
		
		try {
			testPos = new Position("4KC3/5A3/5E3/9/7p1/h8/8H/R5p2/9/5k3");
		} catch (ParseException e) {
			fail("Unable to parse position");
		}

		// Captures
		Move capture = new Move(new Piece(Piece.Color.BLACK, Piece.Type.ROOK),
				new Pair<>(0, 7), new Pair<>(0, 5));
		assertEquals(capture.write(testPos, Move.MoveFormat.ALGEBRAIC), "Rxa5",
				"Captures are indicated with x");
		
		// Checks
		Move check = new Move(new Piece(Piece.Color.BLACK, Piece.Type.ROOK),
				new Pair<>(0, 7), new Pair(0, 9));
		assertEquals(check.write(testPos, Move.MoveFormat.ALGEBRAIC), "Ra1+",
				"Checks are indicted with +");
		
		// Capture with check
		Move chCapt = new Move(new Piece(Piece.Color.BLACK, Piece.Type.HORSE),
				new Pair<>(8, 6), new Pair<>(6, 7));
		assertEquals(chCapt.write(testPos, Move.MoveFormat.ALGEBRAIC), "Hxg3+",
				"Checks and captures can be combined");
		
		// Checkmate
		Move chMate = new Move(new Piece(Piece.Color.BLACK, Piece.Type.ROOK),
				new Pair<>(0, 7), new Pair<>(5, 7));
		assertEquals(chMate.write(testPos, Move.MoveFormat.ALGEBRAIC), "Rf3#",
				"Checkmate is indicated with #");
		
		// Capture with checkmate
		Move captMate = new Move(new Piece(Piece.Color.BLACK, Piece.Type.ELEPHANT),
				new Pair<>(5, 2), new Pair<>(7, 4));
		assertEquals(captMate.write(testPos, Move.MoveFormat.ALGEBRAIC), "Exh6#",
				"Checkmate and captures can be combined");
		
	}

}
