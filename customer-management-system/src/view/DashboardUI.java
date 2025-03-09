package view;

import business.BasketController;
import business.CartController;
import business.CustomerController;
import business.ProductController;
import core.Helper;
import core.Item;
import entity.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class DashboardUI extends JFrame{
    private JPanel container;
    private JLabel lbl_welcome;
    private JButton btn_logout;
    private JTabbedPane tab_menu;
    private JPanel pnl_customer;
    private JScrollPane scrl_customer;
    private JTable tbl_customer;
    private JPanel pnl_customer_filter;
    private JTextField fld_f_customer_name;
    private JComboBox<Customer.TYPE> cmb_f_customer_type;
    private JButton btn_customer_filter;
    private JButton btn_customer_filter_reset;
    private JButton btn_customer_new;
    private JLabel lbl_f_customer_name;
    private JLabel lbl_f_customer_type;
    private JPanel pnl_product;
    private JScrollPane scrl_product;
    private JTable tbl_product;
    private JPanel pnl_product_filter;
    private JTextField fld_f_product_name;
    private JTextField fld_f_product_code;
    private JComboBox<Item> cmb_f_product_stock;
    private JLabel lbl_f_product_name;
    private JLabel lbl_f_product_code;
    private JLabel lbl_f_product_stock;
    private JButton btn_product_filter;
    private JButton btn_product_filter_reset;
    private JButton btn_product_new;
    private JPanel pnl_basket;
    private JPanel pnl_basket_top;
    private JScrollPane scrl_basket;
    private JComboBox<Item> cmb_basket_customer;
    private JLabel lbl_basket_count;
    private JLabel lbl_basket_price;
    private JButton btn_basket_reset;
    private JButton btn_basket_new;
    private JTable tbl_basket;
    private JTable tbl_cart;
    private JScrollPane scrl_cart;
    private User user;
    private CustomerController customerController;
    private ProductController productController;
    private BasketController basketController;
    private CartController cartController;
    private DefaultTableModel tmdl_customer = new DefaultTableModel();
    private DefaultTableModel tmdl_product = new DefaultTableModel();
    private DefaultTableModel tmdl_basket = new DefaultTableModel();
    private DefaultTableModel tmdl_cart = new DefaultTableModel();
    private JPopupMenu jpop_customer = new JPopupMenu();
    private JPopupMenu jpop_product = new JPopupMenu();

    public DashboardUI(User user){
        this.user = user;
        this.customerController = new CustomerController();
        this.productController = new ProductController();
        this.basketController = new BasketController();
        this.cartController = new CartController();
        if (user == null){
            Helper.showMsg("error");
            dispose();
        }
        this.add(container);
        this.setTitle("Müşteri Yönetim Sistemi");
        this.setSize(1000, 500);

        int x = (Toolkit.getDefaultToolkit().getScreenSize().width - this.getSize().width) / 2;
        int y = (Toolkit.getDefaultToolkit().getScreenSize().height - this.getSize().height) / 2;

        this.setLocation(x, y);
        this.setVisible(true);

        this.lbl_welcome.setText("Hoşgeldiniz " + this.user.getName());
        btn_logout.addActionListener(e -> {
            dispose();
            LoginUI loginUI = new LoginUI();
        });

        // CUSTOMER TAB
        loadCustomerTable(null);
        loadCustomerPopMenu();
        loadCustomerButtonEvent();
        this.cmb_f_customer_type.setModel(new DefaultComboBoxModel<>(Customer.TYPE.values()));
        this.cmb_f_customer_type.setSelectedItem(null);

        // PRODUCT TAB
        loadProductTable(null);
        loadProductPopMenu();
        loadProductButtonEvent();
        this.cmb_f_product_stock.addItem(new Item(1, "Stokta var"));
        this.cmb_f_product_stock.addItem(new Item(2, "Stokta yok"));
        this.cmb_f_product_stock.setSelectedItem(null);

        // BASKET TAB
        loadBasketTable();
        loadBasketButtonEvent();
        loadBasketCustomerCombo();

        // CART TAB
        loadCartTable();
    }

    private void loadCartTable() {
        Object[] colCart = {"ID", "Müşteri Adı", "Ürün Adı", "Fiyat", "Sipariş Tarihi", "Not"};
        ArrayList<Cart> carts = this.cartController.findAll();
        DefaultTableModel clearModel = (DefaultTableModel) this.tbl_cart.getModel();
        clearModel.setRowCount(0);
        this.tmdl_cart.setColumnIdentifiers(colCart);
        for (Cart cart : carts){
            String productName = (cart.getProduct() != null) ? cart.getProduct().getName() : "Ürün Yok";
            Object[] rowObject = {
                    cart.getId(),
                    cart.getCustomer().getName(),
                    productName,
                    cart.getPrice(),
                    cart.getDate(),
                    cart.getNote()
            };
            this.tmdl_cart.addRow(rowObject);
        }
        this.tbl_cart.setModel(tmdl_cart);
        this.tbl_cart.getTableHeader().setReorderingAllowed(false);
        this.tbl_cart.getColumnModel().getColumn(0).setMaxWidth(50);
        this.tbl_cart.setEnabled(true);
    }

    private void loadBasketCustomerCombo() {
        ArrayList<Customer> customers = this.customerController.findAll();
        this.cmb_basket_customer.removeAllItems();
        for (Customer customer : customers){
            int comboKey = customer.getId();
            String comboValue = customer.getName();
            this.cmb_basket_customer.addItem(new Item(comboKey, comboValue));
        }
        this.cmb_basket_customer.setSelectedItem(null);
    }

    private void loadBasketButtonEvent() {
        this.btn_basket_reset.addActionListener(e -> {
            if (this.basketController.clear()){
                Helper.showMsg("done");
                loadBasketTable();
            } else {
                Helper.showMsg("error");
            }
        });
        this.btn_basket_new.addActionListener(e -> {
            Item selectCustomer = (Item) this.cmb_basket_customer.getSelectedItem();
            if (selectCustomer == null){
                Helper.showMsg("Lütfen bir müşteri seçiniz!");
            } else {
                Customer customer = this.customerController.getById(selectCustomer.getKey());
                ArrayList<Basket> baskets = this.basketController.findAll();
                if (customer.getId() == 0){
                    Helper.showMsg("Böyle bir müşteri bulunamadı!");
                } else if (baskets.size() == 0) {
                    Helper.showMsg("Lütfen sepete ürün ekleyiniz!");
                } else {
                    CartUI cartUI = new CartUI(customer);
                    cartUI.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            loadBasketTable();
                            loadProductTable(null);
                            loadCartTable();
                        }
                    });
                }
            }
        });
    }

    private void loadBasketTable() {
        Object[] colBasket = {"ID", "Ürün Adı", "Ürün Kodu", "Ürün Stok Adeti", "Fiyat"};
        ArrayList<Basket> baskets = this.basketController.findAll();
        DefaultTableModel clearModel = (DefaultTableModel) this.tbl_basket.getModel();
        clearModel.setRowCount(0);
        this.tmdl_basket.setColumnIdentifiers(colBasket);
        int totalPrice = 0;
        for (Basket basket : baskets){
            Object[] rowObject = {
                    basket.getId(),
                    basket.getProduct().getName(),
                    basket.getProduct().getCode(),
                    basket.getProduct().getStock(),
                    basket.getProduct().getPrice()
            };
            this.tmdl_basket.addRow(rowObject);
            totalPrice += basket.getProduct().getPrice();
        }
        this.lbl_basket_price.setText(totalPrice + " TL");
        this.lbl_basket_count.setText(baskets.size() + " Adet");
        this.tbl_basket.setModel(tmdl_basket);
        this.tbl_basket.getTableHeader().setReorderingAllowed(false);
        this.tbl_basket.getColumnModel().getColumn(0).setMaxWidth(50);
        this.tbl_basket.setEnabled(true);
    }

    private void loadProductButtonEvent() {
        this.btn_product_new.addActionListener(e -> {
            ProductUI productUI = new ProductUI(new Product());
            productUI.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    loadProductTable(null);
                }
            });
        });
        this.btn_product_filter.addActionListener(e -> {
            ArrayList<Product> filteredProducts = this.productController.filter(
                    this.fld_f_product_name.getText(),
                    this.fld_f_product_code.getText(),
                    (Item)this.cmb_f_product_stock.getSelectedItem()
            );
            loadProductTable(filteredProducts);
        });
        this.btn_product_filter_reset.addActionListener(e -> {
            this.fld_f_product_name.setText(null);
            this.fld_f_product_code.setText(null);
            this.cmb_f_product_stock.setSelectedItem(null);
            loadProductTable(null);
        });
    }

    private void loadProductPopMenu(){
        this.tbl_product.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int selectedRow= tbl_product.rowAtPoint(e.getPoint());
                tbl_product.setRowSelectionInterval(selectedRow, selectedRow);
            }
        });
        this.jpop_product.add("Sepete Ekle").addActionListener(e -> {
            int selectId = Integer.parseInt(tbl_product.getValueAt(tbl_product.getSelectedRow(), 0).toString());
            Product basketProduct = this.productController.getById(selectId);
            if (basketProduct.getStock() <= 0){
                Helper.showMsg("Bu üründen stokta kalmadı!");
            } else {
                Basket basket = new Basket(basketProduct.getId());
                if (this.basketController.save(basket)){
                    Helper.showMsg("done");
                    loadBasketTable();
                } else {
                    Helper.showMsg("error");
                }
            }
        });
        this.jpop_product.add("Güncelle").addActionListener(e -> {
            int selectId = Integer.parseInt(tbl_product.getValueAt(tbl_product.getSelectedRow(), 0).toString());
            ProductUI productUI = new ProductUI(this.productController.getById(selectId));
            productUI.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    loadProductTable(null);
                    loadBasketTable();
                }
            });
        });
        this.jpop_product.add("Sil").addActionListener(e -> {
            int selectId = Integer.parseInt(tbl_product.getValueAt(tbl_product.getSelectedRow(), 0).toString());
            if (Helper.confirm("sure")){
                if (this.productController.delete(selectId)){
                    Helper.showMsg("done");
                    loadProductTable(null);
                    loadBasketTable();
                } else {
                    Helper.showMsg("error");
                }
            }
        });
        this.tbl_product.setComponentPopupMenu(this.jpop_product);
    }

    private void loadProductTable(ArrayList<Product> products) {
        Object[] colProduct = {"ID", "Ürün Adı", "Ürün Kodu", "Ürün Stok Adeti", "Fiyat"};
        if (products == null){
            products = this.productController.findAll();
        }
        DefaultTableModel clearModel = (DefaultTableModel) this.tbl_product.getModel();
        clearModel.setRowCount(0);
        this.tmdl_product.setColumnIdentifiers(colProduct);
        for (Product product : products){
            Object[] rowObject = {
                    product.getId(),
                    product.getName(),
                    product.getCode(),
                    product.getStock(),
                    product.getPrice()
            };
            this.tmdl_product.addRow(rowObject);
        }

        this.tbl_product.setModel(tmdl_product);
        this.tbl_product.getTableHeader().setReorderingAllowed(false);
        this.tbl_product.getColumnModel().getColumn(0).setMaxWidth(50);
        this.tbl_product.setEnabled(true);
    }

    private void loadCustomerButtonEvent() {
        this.btn_customer_new.addActionListener(e -> {
            CustomerUI customerUI = new CustomerUI(new Customer());
            customerUI.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    loadCustomerTable(null);
                    loadBasketCustomerCombo();
                }
            });
        });
        this.btn_customer_filter.addActionListener(e -> {
            ArrayList<Customer> filteredCustomers = this.customerController.filter(
                    this.fld_f_customer_name.getText(),
                    (Customer.TYPE) this.cmb_f_customer_type.getSelectedItem()
            );
            loadCustomerTable(filteredCustomers);
        });
        this.btn_customer_filter_reset.addActionListener(e -> {
            loadCustomerTable(null);
            this.fld_f_customer_name.setText(null);
            this.cmb_f_customer_type.setSelectedItem(null);
        });
    }

    private void loadCustomerPopMenu() {
        this.tbl_customer.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int selectedRow= tbl_customer.rowAtPoint(e.getPoint());
                tbl_customer.setRowSelectionInterval(selectedRow, selectedRow);
            }
        });
        this.jpop_customer.add("Güncelle").addActionListener(e -> {
            int selectId = Integer.parseInt(tbl_customer.getValueAt(tbl_customer.getSelectedRow(), 0).toString());
            CustomerUI customerUI = new CustomerUI(this.customerController.getById(selectId));
            customerUI.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    loadCustomerTable(null);
                    loadBasketCustomerCombo();
                }
            });
        });
        this.jpop_customer.add("Sil").addActionListener(e -> {
            int selectId = Integer.parseInt(tbl_customer.getValueAt(tbl_customer.getSelectedRow(), 0).toString());
            if (Helper.confirm("sure")){
                if (this.customerController.delete(selectId)){
                    Helper.showMsg("done");
                    loadCustomerTable(null);
                    loadBasketCustomerCombo();
                } else {
                    Helper.showMsg("error");
                }
            }
        });

        this.tbl_customer.setComponentPopupMenu(this.jpop_customer);
    }

    private void loadCustomerTable(ArrayList<Customer> customers) {
        Object[] colCustomer = {"ID", "Müşteri Adı", "Tipi", "E-Posta", "Telefon", "Adres"};
        if (customers == null){
            customers = this.customerController.findAll();
        }
        DefaultTableModel clearModel = (DefaultTableModel) this.tbl_customer.getModel();
        clearModel.setRowCount(0);
        this.tmdl_customer.setColumnIdentifiers(colCustomer);
        for (Customer customer : customers){
            Object[] rowObject = {
                    customer.getId(),
                    customer.getName(),
                    customer.getType(),
                    customer.getEmail(),
                    customer.getPhone(),
                    customer.getAddress()
            };
            this.tmdl_customer.addRow(rowObject);
        }

        this.tbl_customer.setModel(tmdl_customer);
        this.tbl_customer.getTableHeader().setReorderingAllowed(false);
        this.tbl_customer.getColumnModel().getColumn(0).setMaxWidth(50);
        this.tbl_customer.setEnabled(true);
    }
}
