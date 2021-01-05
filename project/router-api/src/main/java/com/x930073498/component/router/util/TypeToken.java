package com.x930073498.component.router.util;

import java.lang.reflect.*;
import java.util.*;


public class TypeToken<T> {
  final Class<? super T> rawType;
  final Type type;
  final int hashCode;


  @SuppressWarnings("unchecked")
  protected TypeToken() {
    this.type = getSuperclassTypeParameter(getClass());
    this.rawType = (Class<? super T>) Types.getRawType(type);
    this.hashCode = type.hashCode();
  }


  @SuppressWarnings("unchecked")
  TypeToken(Type type) {
    this.type = Types.canonicalize(Preconditions.checkNotNull(type));
    this.rawType = (Class<? super T>) Types.getRawType(this.type);
    this.hashCode = this.type.hashCode();
  }


  static Type getSuperclassTypeParameter(Class<?> subclass) {
    Type superclass = subclass.getGenericSuperclass();
    if (superclass instanceof Class) {
      throw new RuntimeException("Missing type parameter.");
    }
    ParameterizedType parameterized = (ParameterizedType) superclass;
    return Types.canonicalize(parameterized.getActualTypeArguments()[0]);
  }


  public final Class<? super T> getRawType() {
    return rawType;
  }


  public final Type getType() {
    return type;
  }

  public boolean isAssignableFrom(Class<?> cls) {
    return isAssignableFrom((Type) cls);
  }


  public boolean isAssignableFrom(Type from) {
    return isAssignableFrom(type, rawType, from);
  }

  public static boolean isAssignableFrom(Type type, Class<?> rawType, Type from) {
    if (from == null) {
      return false;
    }

    if (type.equals(from)) {
      return true;
    }

    if (type instanceof Class<?>) {

      return rawType.isAssignableFrom(Types.getRawType(from));
    } else if (type instanceof ParameterizedType) {
      return isAssignableFrom(from, (ParameterizedType) type);
    } else if (type instanceof GenericArrayType) {
      return rawType.isAssignableFrom(Types.getRawType(from))
              && isAssignableFrom(from, (GenericArrayType) type);
    } else if (type instanceof WildcardType) {
      return isAssignableFrom(from, (WildcardType) type);
    } else {
      throw buildUnexpectedTypeError(
              type, Class.class, ParameterizedType.class, GenericArrayType.class);
    }
  }

  public static boolean isAssignableFrom(Type from, WildcardType type) {
    Type currentType = type.getUpperBounds()[0];
    Class<?> currentRawType = Types.getRawType(currentType);
    return isAssignableFrom(currentType, currentRawType, from);
  }


  @Deprecated
  public boolean isAssignableFrom(TypeToken<?> token) {
    return isAssignableFrom(token.getType());
  }


  private static boolean isAssignableFrom(Type from, GenericArrayType to) {
    Type toGenericComponentType = to.getGenericComponentType();
    if (toGenericComponentType instanceof ParameterizedType) {
      Type t = from;
      if (from instanceof GenericArrayType) {
        t = ((GenericArrayType) from).getGenericComponentType();
      } else if (from instanceof Class<?>) {
        Class<?> classType = (Class<?>) from;
        while (classType.isArray()) {
          classType = classType.getComponentType();
        }
        t = classType;
      }
      return isAssignableFrom(t, (ParameterizedType) toGenericComponentType);
    }
    // No generic defined on "to"; therefore, return true and let other
    // checks determine assignability
    return true;
  }


  private static boolean isAssignableFrom(Type from, ParameterizedType to) {

    if (from == null) {
      return false;
    }

    if (to.equals(from)) {
      return true;
    }

    Class<?> clazz = Types.getRawType(from);
    ParameterizedType ptype = null;
    if (from instanceof ParameterizedType) {
      ptype = (ParameterizedType) from;
    }

    if (ptype != null) {
      Class<?> toType = Types.getRawType(to);
      if (toType == clazz) {
        Type[] fA = ptype.getActualTypeArguments();
        Type[] tA = to.getActualTypeArguments();
        boolean result = true;
        for (int i = 0; i < fA.length; i++) {
          Type f = fA[i];
          Type t = tA[i];
          result = result && isAssignableFrom(t, Types.getRawType(t), f);
        }
        return result;
      }
    }

    for (Type itype : clazz.getGenericInterfaces()) {
      Class<?> temp = Types.getRawType(itype);
      Type tempType = Types.getSupertype(from, clazz, temp);
      if (isAssignableFrom(to, Types.getRawType(to), tempType)) {
        return true;
      }
    }

    return isAssignableFrom(to, Types.getRawType(to), Types.getSupertype(from, clazz, Types.getRawType(clazz.getGenericSuperclass())));
  }


  private static boolean typeEquals(ParameterizedType from,
                                    ParameterizedType to, Map<String, Type> typeVarMap) {
    if (from.getRawType().equals(to.getRawType())) {
      Type[] fromArgs = from.getActualTypeArguments();
      Type[] toArgs = to.getActualTypeArguments();
      for (int i = 0; i < fromArgs.length; i++) {
        if (!matches(fromArgs[i], toArgs[i], typeVarMap)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private static AssertionError buildUnexpectedTypeError(
          Type token, Class<?>... expected) {

    // Build exception message
    StringBuilder exceptionMessage =
            new StringBuilder("Unexpected type. Expected one of: ");
    for (Class<?> clazz : expected) {
      exceptionMessage.append(clazz.getName()).append(", ");
    }
    exceptionMessage.append("but got: ").append(token.getClass().getName())
            .append(", for type token: ").append(token.toString()).append('.');

    return new AssertionError(exceptionMessage.toString());
  }


  private static boolean matches(Type from, Type to, Map<String, Type> typeMap) {
    return to.equals(from)
            || (from instanceof TypeVariable
            && to.equals(typeMap.get(((TypeVariable<?>) from).getName())));

  }

  @Override
  public final int hashCode() {
    return this.hashCode;
  }

  @Override
  public final boolean equals(Object o) {
    return o instanceof TypeToken<?>
            && Types.equals(type, ((TypeToken<?>) o).type);
  }

  @Override
  public final String toString() {
    return Types.typeToString(type);
  }

  public static TypeToken<?> get(Type type) {
    return new TypeToken<Object>(type);
  }


  public static <T> TypeToken<T> get(Class<T> type) {
    return new TypeToken<T>(type);
  }

  public static TypeToken<?> getParameterized(Type rawType, Type... typeArguments) {
    return new TypeToken<Object>(Types.newParameterizedTypeWithOwner(null, rawType, typeArguments));
  }


  public static TypeToken<?> getArray(Type componentType) {
    return new TypeToken<Object>(Types.arrayOf(componentType));
  }
}
