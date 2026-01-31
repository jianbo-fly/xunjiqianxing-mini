/**
 * Validate - 表单验证
 */

const { REGEX } = require('../config/constants');

/**
 * 验证手机号
 */
const isPhone = (value) => {
  return REGEX.PHONE.test(value);
};

/**
 * 验证身份证号
 */
const isIdCard = (value) => {
  return REGEX.ID_CARD.test(value);
};

/**
 * 验证邮箱
 */
const isEmail = (value) => {
  return REGEX.EMAIL.test(value);
};

/**
 * 验证非空
 */
const isRequired = (value) => {
  if (value === null || value === undefined) return false;
  if (typeof value === 'string') return value.trim() !== '';
  if (Array.isArray(value)) return value.length > 0;
  return true;
};

/**
 * 验证最小长度
 */
const minLength = (value, min) => {
  if (!value) return false;
  return String(value).length >= min;
};

/**
 * 验证最大长度
 */
const maxLength = (value, max) => {
  if (!value) return true;
  return String(value).length <= max;
};

/**
 * 验证范围
 */
const range = (value, min, max) => {
  const num = Number(value);
  return !isNaN(num) && num >= min && num <= max;
};

/**
 * 表单验证器
 */
class Validator {
  constructor() {
    this.rules = [];
    this.errors = [];
  }

  /**
   * 添加验证规则
   */
  add(value, rules) {
    this.rules.push({ value, rules });
    return this;
  }

  /**
   * 执行验证
   */
  validate() {
    this.errors = [];

    for (const item of this.rules) {
      const { value, rules } = item;

      for (const rule of rules) {
        const { type, message, ...params } = rule;
        let isValid = true;

        switch (type) {
          case 'required':
            isValid = isRequired(value);
            break;
          case 'phone':
            isValid = !value || isPhone(value);
            break;
          case 'idCard':
            isValid = !value || isIdCard(value);
            break;
          case 'email':
            isValid = !value || isEmail(value);
            break;
          case 'minLength':
            isValid = minLength(value, params.min);
            break;
          case 'maxLength':
            isValid = maxLength(value, params.max);
            break;
          case 'range':
            isValid = range(value, params.min, params.max);
            break;
          case 'custom':
            isValid = params.validator(value);
            break;
        }

        if (!isValid) {
          this.errors.push(message);
          break; // 一个字段只报一个错误
        }
      }
    }

    return this.errors.length === 0;
  }

  /**
   * 获取第一个错误
   */
  getFirstError() {
    return this.errors[0] || '';
  }

  /**
   * 获取所有错误
   */
  getAllErrors() {
    return this.errors;
  }

  /**
   * 清空规则
   */
  clear() {
    this.rules = [];
    this.errors = [];
    return this;
  }
}

/**
 * 创建验证器
 */
const createValidator = () => new Validator();

/**
 * 快速验证
 */
const validate = (data, rules) => {
  const validator = new Validator();

  for (const field in rules) {
    const value = data[field];
    const fieldRules = rules[field];
    validator.add(value, fieldRules);
  }

  const isValid = validator.validate();

  return {
    isValid,
    error: validator.getFirstError(),
    errors: validator.getAllErrors(),
  };
};

module.exports = {
  isPhone,
  isIdCard,
  isEmail,
  isRequired,
  minLength,
  maxLength,
  range,
  Validator,
  createValidator,
  validate,
};
