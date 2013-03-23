###
Proprietary and confidential.
Copyright $year
All rights reserved.
Author: Daniel Hardman
Created: Sep 1, 2009
###

import:
 java.lang:
  annotation.Annotation
  reflect: Constructor, InvocationTargetException, Method, Modifier, ParameterizedType, Type
 java.net: URI, URL
 java.util: Calendar, Collection, Date, GregorianCalendar, HashMap, Map, Properties, Set
  regex: Matcher, Pattern;
 com.perfectsearchcorp.util: DateUtil, Pair, StrUtil, URIUtil, URLUtil

"""
A class that helps get or set properties on other objects using Java
reflection.
"""
class Property:
 implements: Comparable<Property>
 members:
  "The name of this property."
  name: str -w 
  
  """
  The {@link Method method} that is invoked to get a property value.<br/>
  <br/>
  Normally, this method is found by looking for public methods that have a
  name beginning with "get", take no params, and return a value, where
  those methods have a paired setter that starts with "set" that is of type
  void and takes 1 param that's the same type as the getter returns. This
  behavior can be customized by decorating non-standard getters with a
  {@link CustomProperty} annotation.
  """
  getter: Method -w

  "The {@link Method method} that is invoked to set a property value."
  setter: Method -w

  "The {@link Class class} that contains this property."
  type: Class<?> -w
	
  "Is this property only settable on a command line?"
  cmdline_only: bool -w

  """ If {@link #isCollection()} is true, the type of the item in the
  collection. """
  element_type: Class<?> -w
  
 methods:

  """ Get the value of a property on an object using Java reflection. """
  get(
    "the object on which the {@link #getter getter} should be invoked"
    obj: Object
   ) >> str
   throws: InvocationException
   :
    o: object = getRaw(obj)
    return object_to_string(o, type)

  object_to_string(o:object, type: Class<?>) >> str
   :
    if o == null: return null
    elif type.is_enum():
     e: Enum<?> = (Enum<?>) o
     return "${e}/${e.ordinal}"
    elif o isa Date:
     return DateUtil.format_standard_date((Date) o)
    elif o isa Calendar:
     return DateUtil.format_standard_date(((Calendar) o).get_time())
    else:
     return o.to_string()
     
 members:
  """ Set this flag after annotation is read and cached.
  If this flag is true, do not try to read annotation. """
  annotation: Properties = null

 methods:
  get_annotation() >> str
    read_annotation()
    return annotation
	
  get_attribute(key: str) >> annotation
   :
    return get_annotation().get_property(key)
	
  get_boolean_attribute(key: str) >> bool
   :
    ret: bool = false
    prop: str = get_attribute(key)
    if prop != null:
     ret = Boolean.parseBoolean(prop)
     return ret

  handle_annotation(annotation) +private >> void
   :
    methods: Method[] = a.class.methods
    for m: method in methods:
     try:
      if len(m.parameter_types) == 0:
       key: str = m.getName()
       value: Object 
       value = m.invoke(a, (Object[])null)
       if value != null:
        value_str: str = object_to_string(value, value.class)
	if !StrUtil.isNullOrEmpty(valueStr)):
	 annotation.set_property(key, valueStr)
     catch:
      IllegalArgumentException:
      IllegalAccessException:
      InvocationTargetException:
	
  "Return the descrip from an annotation."
  read_annotation() +private >> void
   :
    if annotation:
     return
    annotation = new Properties()
    for i: int; i < 2; ++i:
     m: method = (i == 0) ? getter : setter
     if m:
      ann: Annotation[] = m.annotations
      if ann:
       for a in ann:
        handle_annotation(a)
