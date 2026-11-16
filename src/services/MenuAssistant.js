class MenuAssistant {
  constructor(menu) { this.menu = menu; }
  // Basic matching only; this is not an actual AI integration.
  recommendItems(preferences = '') {
    const keywords = preferences.toLowerCase().split(/\W+/).filter(Boolean);
    return this.menu.getItems().filter(item => keywords.some(k =>
      [item.name, item.description, item.category, ...item.tags].join(' ').toLowerCase().includes(k)
    ));
  }
}
module.exports = MenuAssistant;
