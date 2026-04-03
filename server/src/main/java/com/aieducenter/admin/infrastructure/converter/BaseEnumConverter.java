package com.aieducenter.admin.infrastructure.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

import com.cartisan.core.domain.BaseEnum;

/**
 * BaseEnum Converter Factory。
 *
 * 将 String/Integer 参数转换为 BaseEnum 枚举类型。
 *
 * <p>支持 @RequestParam、@PathVariable 等场景直接使用枚举类型参数：</p>
 * <pre>
 * {@code
 * public void updateStatus(
 *         @PathVariable Long id,
 *         @RequestParam AdminUserStatus status) {  // 自动转换 Integer → AdminUserStatus
 *     // ...
 * }
 * }
 * </pre>
 *
 * @since 0.1.0
 */
@Component
public class BaseEnumConverter implements ConverterFactory<String, BaseEnum<?>> {

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BaseEnum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToBaseEnumConverter<>(targetType);
    }

    /**
     * String → BaseEnum Converter。
     */
    private static class StringToBaseEnumConverter<T extends BaseEnum<?>> implements Converter<String, T> {

        private final Class<T> enumType;

        public StringToBaseEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String source) {
            if (source == null || source.isEmpty()) {
                return null;
            }

            // 支持两种输入格式：Integer code 或 String name
            try {
                Integer code = Integer.valueOf(source);
                // 手动遍历枚举值匹配 code
                for (T enumConstant : enumType.getEnumConstants()) {
                    if (enumConstant.getCode().equals(code)) {
                        return enumConstant;
                    }
                }
                throw new IllegalArgumentException(
                    "No enum constant " + enumType.getSimpleName() + " with code " + code
                );
            } catch (NumberFormatException e) {
                // 如果不是数字，尝试按名称匹配（不推荐，但兼容）
                for (T enumConstant : enumType.getEnumConstants()) {
                    if (enumConstant.getName().equals(source)) {
                        return enumConstant;
                    }
                }
                throw new IllegalArgumentException(
                    "Invalid value '" + source + "' for enum " + enumType.getSimpleName()
                    + ". Expected Integer code or enum name."
                );
            }
        }
    }
}
