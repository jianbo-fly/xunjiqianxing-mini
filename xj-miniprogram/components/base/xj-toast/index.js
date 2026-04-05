Component({
  data: {
    visible: false,
    type: 'info',    // info | success | error | warning
    message: '',
  },

  methods: {
    /**
     * 显示 Toast
     * @param {object} options
     * @param {string} options.message  提示文字
     * @param {string} [options.type]   类型: info / success / error / warning
     * @param {number} [options.duration] 显示时长(ms)，默认 2000
     */
    show({ message, type = 'info', duration = 2000 }) {
      if (this._hideTimer) {
        clearTimeout(this._hideTimer);
        this._hideTimer = null;
      }
      this.setData({ message, type, visible: true });
      this._hideTimer = setTimeout(() => {
        this.setData({ visible: false });
        this._hideTimer = null;
      }, duration);
    },

    hide() {
      if (this._hideTimer) {
        clearTimeout(this._hideTimer);
        this._hideTimer = null;
      }
      this.setData({ visible: false });
    },
  },
});
