import java.util.*;

/**
 * This class handles the range header sent from the client.
 * @author Xiaoxiong Li, Di Wu
 *
 */
public class RangeHeader {
    public class Range {
        private long startIdx;
        private long endIdx;
        public Range(long start, long end) {
            startIdx = start;
            endIdx = end;
        }
        public long getStart() {
            return startIdx;
        }
        public long getEnd() {
            return endIdx;
        }
    }
    private boolean isValid;
    private boolean isSatisfiable;
    public ArrayList<Range> ranges;
    public RangeHeader(String header, long fileLength) {
        //parse the header to get ranges
        int idx = header.indexOf("bytes=");
        if (idx != 0) {
            isValid = false;
        } else {
            isValid = true;
            isSatisfiable = false;
            header = header.substring(6);
            String[] rangeSet = header.split(",");         
            for (String r : rangeSet) {
                int index = r.indexOf("-");
                if (index == 0) {
                    long suffix = Integer.valueOf(r.substring(1));
                    if (suffix != 0) {
                        ranges.add(new Range(fileLength - suffix, fileLength - 1));
                        isSatisfiable = true;
                    }
                } else if (index == r.length() - 1) {
                    long start = Integer.valueOf(r.substring(0, index));
                    if (start < fileLength) {
                        ranges.add(new Range(start, fileLength - 1));
                        isSatisfiable = true;
                    }
                } else {
                    long start = Long.valueOf(r.substring(0, index));
                    long end = Long.valueOf(r.substring(index + 1));
                    if (start > end) {
                        isValid = false;
                    } else {
                        if (start < fileLength) {
                            ranges.add(new Range(start, end));
                            isSatisfiable = true;
                        }
                    }
                }
            }
            if (!isValid) {
                isSatisfiable = false;
            }
        }
    }
    public boolean isValid() {
        return isValid;
    }
    public boolean isSatisfiable() {
        return isSatisfiable;
    }
}
