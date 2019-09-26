package lt.bit.java2.SpringJPA.utility;

public class PaginationRange {
    private Integer rangeFrom;
    private Integer rangeTo;

    public PaginationRange(int pageNumber, int pageCount) {
        if (pageNumber == 1) {
            this.rangeFrom = 1;
            this.rangeTo = 5;
        } else if (pageNumber == 2) {
            this.rangeFrom = 1;
            this.rangeTo = 5;
        } else if (pageNumber == 3) {
            this.rangeFrom = 1;
            this.rangeTo = 5;
        } else if (pageNumber == pageCount - 2) {
            this.rangeFrom = pageCount - 4;
            this.rangeTo = pageCount;
        } else if (pageNumber == pageCount - 1) {
            this.rangeFrom = pageCount - 4;
            this.rangeTo = pageCount;
        } else if (pageNumber == pageCount) {
            this.rangeFrom = pageCount - 4;
            this.rangeTo = pageCount;
        } else {
            this.rangeFrom = pageNumber - 2;
            this.rangeTo = pageNumber + 2;
        }
    }

    public PaginationRange(int pageNumber, int pageCount, int buttonCount) {

        /*Current BUG:
        * If buttonCount is even number the button count jumps +1 (to an odd number)
        *  when selecting not first and not last pages. Could be because of thymeleaf code in employee-list-page.html
        * If button count is odd number everything works as intended.*/

        int halfButtonCount = (int) Math.floor(buttonCount / 2);

            if (pageNumber <= halfButtonCount) {
                this.rangeFrom = 1;
                this.rangeTo = buttonCount;
            } else if (pageNumber > pageCount - halfButtonCount) {
                this.rangeFrom = pageCount - (buttonCount - 1);
                this.rangeTo = pageCount;
            }else {
                this.rangeFrom = pageNumber - halfButtonCount;
                this.rangeTo = pageNumber + halfButtonCount;
            }
    }

    public Integer getRangeFrom() {
        return rangeFrom;
    }

    public Integer getRangeTo() {
        return rangeTo;
    }
}
