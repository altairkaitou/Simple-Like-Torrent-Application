import java.util.BitSet;
public class FilePieceTracker {
    private BitSet piecesDownloaded;
    private int totalPieces;

    public synchronized void markPieceAsDownloaded(int pieceIndex) {
        piecesDownloaded.set(pieceIndex);
    }

    public synchronized boolean isPieceDownloaded(int pieceIndex) {
        return piecesDownloaded.get(pieceIndex);
    }

    public synchronized boolean areAllPiecesDownloaded() {
        return piecesDownloaded.cardinality() == totalPieces;
    }

    public int getTotalPieces() {
        return totalPieces;
    }
}
