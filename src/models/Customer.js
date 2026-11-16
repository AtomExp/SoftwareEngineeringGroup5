class Customer {
  constructor(customerId, name = 'Guest') { this.customerId = customerId; this.name = name; }
  viewMenu(menu) { return menu.getItems(); }
  checkout(controller) { return controller.onSubmitOrder(); }
}
module.exports = Customer;
