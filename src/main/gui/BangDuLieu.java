package gui;

import java.awt.BorderLayout;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class BangDuLieu<T> extends JPanel {
    private List<T> cacDong = List.of();
    private final JTable bang;
    private final AbstractTableModel moHinhBang;
    private final TableRowSorter<AbstractTableModel> boSapXep;

    public BangDuLieu(String[] cacCot, Function<T, Object[]> cacGiaTri) {
        super(new BorderLayout(0, 8));
        moHinhBang = new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return cacDong.size();
            }

            @Override
            public int getColumnCount() {
                return cacCot.length;
            }

            @Override
            public String getColumnName(int chiSo) {
                return cacCot[chiSo];
            }

            @Override
            public Object getValueAt(int dong, int cot) {
                return cacGiaTri.apply(cacDong.get(dong))[cot];
            }

            @Override
            public Class<?> getColumnClass(int cot) {
                for (T dong : cacDong) {
                    Object giaTri = cacGiaTri.apply(dong)[cot];
                    if (giaTri != null)
                        return giaTri.getClass();
                }
                return Object.class;
            }

            @Override
            public boolean isCellEditable(int dong, int cot) {
                return false;
            }
        };
        bang = new JTable(moHinhBang);
        bang.setRowHeight(32);
        bang.setFillsViewportHeight(true);
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.getTableHeader().setReorderingAllowed(false);
        boSapXep = new TableRowSorter<>(moHinhBang);
        bang.setRowSorter(boSapXep);
        add(new JScrollPane(bang), BorderLayout.CENTER);
    }

    public void capNhatDuLieu(List<T> cacGiaTri) {
        cacDong = List.copyOf(cacGiaTri);
        moHinhBang.fireTableDataChanged();
        bang.clearSelection();
    }

    public T layDongDangChon() {
        int chiSo = bang.getSelectedRow();
        if (chiSo < 0)
            throw new IllegalArgumentException("Chọn một dòng trong bảng trước.");
        return cacDong.get(bang.convertRowIndexToModel(chiSo));
    }

    public void khiChonDong(Consumer<T> xuLyLuaChon) {
        bang.getSelectionModel().addListSelectionListener(suKien -> {
            if (!suKien.getValueIsAdjusting() && bang.getSelectedRow() >= 0)
                xuLyLuaChon.accept(layDongDangChon());
        });
    }

    public void boChonDong() {
        bang.clearSelection();
    }

    public int soDongHienThi() {
        return bang.getRowCount();
    }

    public void ganTimKiem(JTextField oTimKiem) {
        ganTimKiem(oTimKiem, null);
    }

    /** Màn hình có thể chỉ định các thuộc tính được tìm, độc lập cột hiển thị. */
    public void ganTimKiem(JTextField oTimKiem, Function<T, String[]> cacTruongTimKiem) {
        oTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            private void locDuLieu() {
                // Thay điều kiện tìm kiếm thì không giữ kết quả cũ trên màn hình.
                capNhatDuLieu(List.of());
                String vanBan = oTimKiem.getText().trim();
                if (vanBan.isEmpty()) {
                    boSapXep.setRowFilter(RowFilter.regexFilter("(?!)"));
                } else if (cacTruongTimKiem == null) {
                    boSapXep.setRowFilter(
                            RowFilter.regexFilter("(?iu)" + java.util.regex.Pattern.quote(vanBan)));
                } else {
                    var mau = java.util.regex.Pattern.compile(java.util.regex.Pattern.quote(vanBan),
                            java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE);
                    boSapXep.setRowFilter(new RowFilter<AbstractTableModel, Integer>() {
                        @Override
                        public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> muc) {
                            T dong = cacDong.get(muc.getIdentifier());
                            for (String giaTri : cacTruongTimKiem.apply(dong)) {
                                if (giaTri != null && mau.matcher(giaTri).find())
                                    return true;
                            }
                            return false;
                        }
                    });
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                locDuLieu();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                locDuLieu();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                locDuLieu();
            }
        });
        boSapXep.setRowFilter(RowFilter.regexFilter("(?!)"));
    }
}
