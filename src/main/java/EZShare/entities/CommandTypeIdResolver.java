package EZShare.entities;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;

/**
 * To resolve command type.
 * Reference:
 * https://www.thomaskeller.biz/blog/2013/09/10/custom-polymorphic-type-handling-with-jackson/
 *
 * Created by xierch on 2017/3/23.
 */
public class CommandTypeIdResolver implements TypeIdResolver {
    private static final String COMMAND_PACKAGE = Command.class.getPackage().getName();

    private JavaType baseType;

    @Override
    public void init(JavaType baseType) {
        this.baseType = baseType;
    }

    @Override
    public String idFromValue(Object value) {
        return idFromValueAndType(value, value.getClass());
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        if (!Command.class.isAssignableFrom(suggestedType))
            throw new IllegalStateException(suggestedType.getName() + "is not a command class.");
        return suggestedType.getSimpleName().toUpperCase();
    }

    @Override
    public String idFromBaseType() {
        return idFromValueAndType(null, baseType.getRawClass());
    }

    @Override
    public JavaType typeFromId(String id) {
        Class type;
        String className = id.substring(0, 1).toUpperCase() + id.substring(1).toLowerCase();
        try {
            type = ClassUtil.findClass(COMMAND_PACKAGE + "." + className);
        } catch (ClassNotFoundException e) {
            // Return null will cause Jackson throws JsonMappingException.
            return null;
        }
        return TypeFactory.defaultInstance().constructSpecializedType(baseType, type);
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }
}
