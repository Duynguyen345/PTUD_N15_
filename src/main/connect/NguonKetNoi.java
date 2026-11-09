package connect;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface NguonKetNoi {
    Connection moKetNoi() throws SQLException;
}
