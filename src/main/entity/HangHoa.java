package entity;

public record HangHoa(String ma, String ten, String donVi, int ton, int toiThieu) {
    public boolean canBoSung() {
        return ton <= toiThieu;
    }
}
