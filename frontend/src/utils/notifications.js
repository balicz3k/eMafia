/**
 * Prosty system notyfikacji jako zamiennik react-toastify
 * Używa natywnych alertów przeglądarki lub może być rozszerzony o custom UI
 */

class NotificationManager {
  constructor() {
    this.notifications = [];
    this.container = null;
    this.init();
  }

  init() {
    // Sprawdź czy jesteśmy w przeglądarce
    if (typeof window === 'undefined') return;

    // Utwórz kontener dla notyfikacji
    this.container = document.createElement('div');
    this.container.id = 'notification-container';
    this.container.style.cssText = `
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 10000;
      display: flex;
      flex-direction: column;
      gap: 10px;
      max-width: 400px;
    `;
    document.body.appendChild(this.container);
  }

  show(message, type = 'info', options = {}) {
    const { autoClose = 3000 } = options;

    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.style.cssText = `
      padding: 15px 20px;
      border-radius: 8px;
      color: white;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      font-size: 14px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
      animation: slideIn 0.3s ease-out;
      cursor: pointer;
      transition: all 0.3s ease;
      ${this.getTypeStyles(type)}
    `;

    notification.textContent = message;

    // Dodaj style animacji
    if (!document.getElementById('notification-styles')) {
      const style = document.createElement('style');
      style.id = 'notification-styles';
      style.textContent = `
        @keyframes slideIn {
          from {
            transform: translateX(400px);
            opacity: 0;
          }
          to {
            transform: translateX(0);
            opacity: 1;
          }
        }
        @keyframes slideOut {
          from {
            transform: translateX(0);
            opacity: 1;
          }
          to {
            transform: translateX(400px);
            opacity: 0;
          }
        }
        .notification:hover {
          transform: scale(1.02);
          box-shadow: 0 6px 16px rgba(0, 0, 0, 0.4);
        }
      `;
      document.head.appendChild(style);
    }

    // Kliknięcie zamyka notyfikację
    notification.onclick = () => this.remove(notification);

    this.container.appendChild(notification);

    // Auto-close
    if (autoClose) {
      setTimeout(() => this.remove(notification), autoClose);
    }

    return notification;
  }

  getTypeStyles(type) {
    const styles = {
      success: 'background: linear-gradient(135deg, #4CAF50, #66BB6A);',
      error: 'background: linear-gradient(135deg, #F44336, #E53935);',
      warning: 'background: linear-gradient(135deg, #FF9800, #FB8C00);',
      info: 'background: linear-gradient(135deg, #2196F3, #42A5F5);'
    };
    return styles[type] || styles.info;
  }

  remove(notification) {
    notification.style.animation = 'slideOut 0.3s ease-out';
    setTimeout(() => {
      if (notification.parentNode) {
        notification.parentNode.removeChild(notification);
      }
    }, 300);
  }

  success(message, options) {
    return this.show(message, 'success', options);
  }

  error(message, options) {
    return this.show(message, 'error', options);
  }

  warning(message, options) {
    return this.show(message, 'warning', options);
  }

  info(message, options) {
    return this.show(message, 'info', options);
  }
}

// Singleton instance
const toast = new NotificationManager();

export default toast;
