/*
 * See LICENSE file in distribution for copyright and licensing information.
 */
package ioke.lang;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashSet;

import ioke.lang.exceptions.ControlFlow;
import ioke.lang.util.StringUtils;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class DefaultBehavior {
    public static IokeObject signal(Object datum, List<Object> positionalArgs, Map<String, Object> keywordArgs, IokeObject message, IokeObject context) throws ControlFlow {
        IokeObject newCondition = null;
        if(Text.isText(datum)) {
            newCondition = IokeObject.as(context.runtime.condition.getCell(message, context, "Default")).mimic(message, context);
            newCondition.setCell("context", context);
            newCondition.setCell("text", datum);
        } else {
            if(keywordArgs.size() == 0) {
                newCondition = IokeObject.as(datum);
            } else {
                newCondition = IokeObject.as(datum).mimic(message, context);
                newCondition.setCell("context", context);
                for(Map.Entry<String,Object> val : keywordArgs.entrySet()) {
                    String s = val.getKey();
                    newCondition.setCell(s.substring(0, s.length()-1), val.getValue());
                }
            }
        }

        Runtime.RescueInfo rescue = context.runtime.findActiveRescueFor(newCondition);

        List<Runtime.HandlerInfo> handlers = context.runtime.findActiveHandlersFor(newCondition, (rescue == null) ? new Runtime.BindIndex(-1,-1) : rescue.index);
        
        for(Runtime.HandlerInfo rhi : handlers) {
            context.runtime.callMessage.sendTo(context, context.runtime.handlerMessage.sendTo(context, rhi.handler), newCondition);
        }

        if(rescue != null) {
            throw new ControlFlow.Rescue(rescue, newCondition);
        }
                    
        return newCondition;
    }

    public static void init(IokeObject obj) {
        final Runtime runtime = obj.runtime;
        obj.setKind("DefaultBehavior");

        obj.setCell("=",         runtime.base.getCells().get("="));
        obj.setCell("==",        runtime.base.getCells().get("=="));
        obj.setCell("cell",      runtime.base.getCells().get("cell"));
        obj.setCell("cell?",     runtime.base.getCells().get("cell?"));
        obj.setCell("cell=",     runtime.base.getCells().get("cell="));
        obj.setCell("cells",     runtime.base.getCells().get("cells"));
        obj.setCell("cellNames", runtime.base.getCells().get("cellNames"));
        obj.setCell("documentation", runtime.base.getCells().get("documentation"));

        obj.registerMethod(runtime.newJavaMethod("expects two arguments, the first unevaluated, the second evaluated. the first argument should be the name of a cell. if that cell doesn't exist or the value it contains is not true, that cell will be set to the second argument, otherwise nothing will happen. the second argument will NOT be evaluated if the place is not assigned. the result of the expression is the value of the cell. it will use = for this assignment. this method also work together with forms such as []=.", new JavaMethod("||=") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("place")
                    .withRequiredPositional("else")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    IokeObject m1 = IokeObject.as(Message.getArg1(message));
                    String name = m1.getName();

                    if(m1.getArgumentCount() == 0) {
                        Object val = IokeObject.findCell(on, message, context, name);
                        if(val == context.runtime.nul || !IokeObject.isTrue(val)) {
                            return context.runtime.setValue.sendTo(context, on, m1, Message.getArg2(message));
                        } else {
                            return val;
                        }
                    } else {
                        Object val = m1.sendTo(context, on);
                        if(val == context.runtime.nul || !IokeObject.isTrue(val)) {
                            return context.runtime.setValue.sendTo(context, on, m1, Message.getArg2(message));
                        } else {
                            return val;
                        }
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects two arguments, the first unevaluated, the second evaluated. the first argument should be the name of a cell. if that cell exist and the value it contains is a true one, that cell will be set to the second argument, otherwise nothing will happen. the second argument will NOT be evaluated if the place is not assigned. the result of the expression is the value of the cell. it will use = for this assignment. this method also work together with forms such as []=.", new JavaMethod("&&=") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("place")
                    .withRequiredPositional("then")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    IokeObject m1 = IokeObject.as(Message.getArg1(message));
                    String name = m1.getName();

                    if(m1.getArgumentCount() == 0) {
                        Object val = IokeObject.findCell(on, message, context, name);
                        if(val == context.runtime.nul || !IokeObject.isTrue(val)) {
                            return val;
                        } else {
                            return context.runtime.setValue.sendTo(context, on, m1, Message.getArg2(message));
                        }
                    } else {
                        Object val = m1.sendTo(context, on);
                        if(val == context.runtime.nul || !IokeObject.isTrue(val)) {
                            return val;
                        } else {
                            return context.runtime.setValue.sendTo(context, on, m1, Message.getArg2(message));
                        }
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects two arguments, the first unevaluated, the second evaluated. the first argument should be the name of a cell. the value of that cell will be retreived and then the + method will be called on it. finally, the result of the call to + will be assigned to the same name in the current scope. it will use = for this assignment. the result of the expression is the same as the result of the assignment. this method also work together with forms such as []=.", new JavaMethod("+=") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("place")
                    .withRequiredPositional("addend")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    IokeObject m1 = IokeObject.as(Message.getArg1(message));
                    String name = m1.getName();
                    if(m1.getArgumentCount() == 0) {
                        Object val = IokeObject.getCell(on, message, context, name);
                        Object result = context.runtime.plusMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    } else {
                        Object val = m1.sendTo(context, on);
                        Object result = context.runtime.plusMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects two arguments, the first unevaluated, the second evaluated. the first argument should be the name of a cell. the value of that cell will be retreived and then the - method will be called on it. finally, the result of the call to - will be assigned to the same name in the current scope. it will use = for this assignment. the result of the expression is the same as the result of the assignment. this method also work together with forms such as []=.", new JavaMethod("-=") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("place")
                    .withRequiredPositional("subtrahend")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    IokeObject m1 = IokeObject.as(Message.getArg1(message));
                    String name = m1.getName();
                    if(m1.getArgumentCount() == 0) {
                        Object val = IokeObject.getCell(on, message, context, name);
                        Object result = context.runtime.minusMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    } else {
                        Object val = m1.sendTo(context, on);
                        Object result = context.runtime.minusMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects two arguments, the first unevaluated, the second evaluated. the first argument should be the name of a cell. the value of that cell will be retreived and then the * method will be called on it. finally, the result of the call to * will be assigned to the same name in the current scope. it will use = for this assignment. the result of the expression is the same as the result of the assignment. this method also work together with forms such as []=.", new JavaMethod("*=") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("place")
                    .withRequiredPositional("multiplier")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    IokeObject m1 = IokeObject.as(Message.getArg1(message));
                    String name = m1.getName();
                    if(m1.getArgumentCount() == 0) {
                        Object val = IokeObject.getCell(on, message, context, name);
                        Object result = context.runtime.multMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    } else {
                        Object val = m1.sendTo(context, on);
                        Object result = context.runtime.multMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects two arguments, the first unevaluated, the second evaluated. the first argument should be the name of a cell. the value of that cell will be retreived and then the / method will be called on it. finally, the result of the call to / will be assigned to the same name in the current scope. it will use = for this assignment. the result of the expression is the same as the result of the assignment. this method also work together with forms such as []=.", new JavaMethod("/=") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("place")
                    .withRequiredPositional("divisor")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    IokeObject m1 = IokeObject.as(Message.getArg1(message));
                    String name = m1.getName();
                    if(m1.getArgumentCount() == 0) {
                        Object val = IokeObject.getCell(on, message, context, name);
                        Object result = context.runtime.divMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    } else {
                        Object val = m1.sendTo(context, on);
                        Object result = context.runtime.divMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects two arguments, the first unevaluated, the second evaluated. the first argument should be the name of a cell. the value of that cell will be retreived and then the % method will be called on it. finally, the result of the call to % will be assigned to the same name in the current scope. it will use = for this assignment. the result of the expression is the same as the result of the assignment. this method also work together with forms such as []=.", new JavaMethod("%=") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("place")
                    .withRequiredPositional("divisor")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    IokeObject m1 = IokeObject.as(Message.getArg1(message));
                    String name = m1.getName();
                    if(m1.getArgumentCount() == 0) {
                        Object val = IokeObject.getCell(on, message, context, name);
                        Object result = context.runtime.modMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    } else {
                        Object val = m1.sendTo(context, on);
                        Object result = context.runtime.modMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects two arguments, the first unevaluated, the second evaluated. the first argument should be the name of a cell. the value of that cell will be retreived and then the ** method will be called on it. finally, the result of the call to ** will be assigned to the same name in the current scope. it will use = for this assignment. the result of the expression is the same as the result of the assignment. this method also work together with forms such as []=.", new JavaMethod("**=") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("place")
                    .withRequiredPositional("exponent")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    IokeObject m1 = IokeObject.as(Message.getArg1(message));
                    String name = m1.getName();
                    if(m1.getArgumentCount() == 0) {
                        Object val = IokeObject.getCell(on, message, context, name);
                        Object result = context.runtime.expMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    } else {
                        Object val = m1.sendTo(context, on);
                        Object result = context.runtime.expMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects two arguments, the first unevaluated, the second evaluated. the first argument should be the name of a cell. the value of that cell will be retreived and then the & method will be called on it. finally, the result of the call to & will be assigned to the same name in the current scope. it will use = for this assignment. the result of the expression is the same as the result of the assignment. this method also work together with forms such as []=.", new JavaMethod("&=") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("place")
                    .withRequiredPositional("other")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    IokeObject m1 = IokeObject.as(Message.getArg1(message));
                    String name = m1.getName();
                    if(m1.getArgumentCount() == 0) {
                        Object val = IokeObject.getCell(on, message, context, name);
                        Object result = context.runtime.binAndMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    } else {
                        Object val = m1.sendTo(context, on);
                        Object result = context.runtime.binAndMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects two arguments, the first unevaluated, the second evaluated. the first argument should be the name of a cell. the value of that cell will be retreived and then the | method will be called on it. finally, the result of the call to | will be assigned to the same name in the current scope. it will use = for this assignment. the result of the expression is the same as the result of the assignment. this method also work together with forms such as []=.", new JavaMethod("|=") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("place")
                    .withRequiredPositional("other")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    IokeObject m1 = IokeObject.as(Message.getArg1(message));
                    String name = m1.getName();
                    if(m1.getArgumentCount() == 0) {
                        Object val = IokeObject.getCell(on, message, context, name);
                        Object result = context.runtime.binOrMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    } else {
                        Object val = m1.sendTo(context, on);
                        Object result = context.runtime.binOrMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects two arguments, the first unevaluated, the second evaluated. the first argument should be the name of a cell. the value of that cell will be retreived and then the ^ method will be called on it. finally, the result of the call to ^ will be assigned to the same name in the current scope. it will use = for this assignment. the result of the expression is the same as the result of the assignment. this method also work together with forms such as []=.", new JavaMethod("^=") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("place")
                    .withRequiredPositional("other")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    IokeObject m1 = IokeObject.as(Message.getArg1(message));
                    String name = m1.getName();
                    if(m1.getArgumentCount() == 0) {
                        Object val = IokeObject.getCell(on, message, context, name);
                        Object result = context.runtime.binXorMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    } else {
                        Object val = m1.sendTo(context, on);
                        Object result = context.runtime.binXorMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects two arguments, the first unevaluated, the second evaluated. the first argument should be the name of a cell. the value of that cell will be retreived and then the << method will be called on it. finally, the result of the call to << will be assigned to the same name in the current scope. it will use = for this assignment. the result of the expression is the same as the result of the assignment. this method also work together with forms such as []=.", new JavaMethod("<<=") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("place")
                    .withRequiredPositional("other")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    IokeObject m1 = IokeObject.as(Message.getArg1(message));
                    String name = m1.getName();
                    if(m1.getArgumentCount() == 0) {
                        Object val = IokeObject.getCell(on, message, context, name);
                        Object result = context.runtime.lshMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    } else {
                        Object val = m1.sendTo(context, on);
                        Object result = context.runtime.lshMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects two arguments, the first unevaluated, the second evaluated. the first argument should be the name of a cell. the value of that cell will be retreived and then the >> method will be called on it. finally, the result of the call to >> will be assigned to the same name in the current scope. it will use = for this assignment. the result of the expression is the same as the result of the assignment. this method also work together with forms such as []=.", new JavaMethod(">>=") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("place")
                    .withRequiredPositional("other")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    IokeObject m1 = IokeObject.as(Message.getArg1(message));
                    String name = m1.getName();
                    if(m1.getArgumentCount() == 0) {
                        Object val = IokeObject.getCell(on, message, context, name);
                        Object result = context.runtime.rshMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    } else {
                        Object val = m1.sendTo(context, on);
                        Object result = context.runtime.rshMessage.sendTo(context, val, Message.getArg2(message));
                        return context.runtime.setValue.sendTo(context, on, m1, context.runtime.createMessage(Message.wrap(IokeObject.as(result))));
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("returns a text hex representation of the receiver in upper case hex literal, starting with 0x. This value is based on System.identityHashCode, and as such is not totally guaranteed to be totally unique. but almost.", new JavaMethod.WithNoArguments("uniqueHexId") {
                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().getEvaluatedArguments(context, message, on, new ArrayList<Object>(), new HashMap<String, Object>());

                    return context.runtime.newText("0x" + Integer.toHexString(System.identityHashCode(on)).toUpperCase());
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("returns true if the evaluated argument is the same reference as the receiver, false otherwise.", new JavaMethod("same?") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("other")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());
                    return (on == args.get(0)) ? context.runtime._true : context.runtime._false;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("returns false if the left hand side is equal to the right hand side. exactly what this means depend on the object. the default behavior of Ioke objects is to only be equal if they are the same instance.", new JavaMethod("!=") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("other")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());
                    return !IokeObject.equals(on, message.getEvaluatedArgument(0, context)) ? context.runtime._true : context.runtime._false ;
                }
            }));


        obj.registerMethod(runtime.newJavaMethod("breaks out of the enclosing context. if an argument is supplied, this will be returned as the result of the object breaking out of", new JavaMethod("break") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withOptionalPositional("value", "nil")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());

                    Object value = runtime.nil;
                    if(message.getArgumentCount() > 0) {
                        value = message.getEvaluatedArgument(0, context);
                    }
                    throw new ControlFlow.Break(value);
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("returns from the enclosing method/macro. if an argument is supplied, this will be returned as the result of the method/macro breaking out of.", new JavaMethod("return") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withOptionalPositional("value", "nil")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    Object value = runtime.nil;
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());
                    if(args.size() > 0) {
                        value = args.get(0);
                    }
                    IokeObject ctx = context;
                    while(ctx instanceof LexicalContext) {
                        ctx = ((LexicalContext)ctx).surroundingContext;
                    }

                    throw new ControlFlow.Return(value, ctx);
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("returns a new message with the name given as argument to this method.", new JavaMethod("message") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("name")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());
                    Object o = args.get(0);
                    
                    String name = null;
                    if(IokeObject.data(o) instanceof Text) {
                        name = Text.getText(o);
                    } else {
                        name = Text.getText(context.runtime.asText.sendTo(context, o));
                    }

                    Message m = new Message(context.runtime, name);
                    IokeObject ret = context.runtime.createMessage(m);
                    Message.copySourceLocation(message, ret);
                    return ret;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("breaks out of the enclosing context and continues from that point again.", new JavaMethod.WithNoArguments("continue") {
                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().getEvaluatedArguments(context, message, on, new ArrayList<Object>(), new HashMap<String, Object>());

                    throw new ControlFlow.Continue();
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("until the first argument evaluates to something true, loops and evaluates the next argument", new JavaMethod("until") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withOptionalPositionalUnevaluated("condition")
                    .withRestUnevaluated("body")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    if(message.getArgumentCount() == 0) {
                        return runtime.nil;
                    }

                    boolean body = message.getArgumentCount() > 1;
                    Object ret = runtime.nil;
                    boolean doAgain = false;
                    do {
                        doAgain = false;
                        try {
                            while(!IokeObject.isTrue(message.getEvaluatedArgument(0, context))) {
                                if(body) {
                                    ret = message.getEvaluatedArgument(1, context);
                                }
                            }
                        } catch(ControlFlow.Break e) {
                            ret = e.getValue();
                        } catch(ControlFlow.Continue e) {
                            doAgain = true;
                        }
                    } while(doAgain);

                    return ret;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("while the first argument evaluates to something true, loops and evaluates the next argument", new JavaMethod("while") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withOptionalPositionalUnevaluated("condition")
                    .withRestUnevaluated("body")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    if(message.getArgumentCount() == 0) {
                        return runtime.nil;
                    }

                    boolean body = message.getArgumentCount() > 1;
                    Object ret = runtime.nil;
                    boolean doAgain = false;
                    do {
                        doAgain = false;
                        try {
                            while(IokeObject.isTrue(message.getEvaluatedArgument(0, context))) {
                                if(body) {
                                    ret = message.getEvaluatedArgument(1, context);
                                }
                            }
                        } catch(ControlFlow.Break e) {
                            ret = e.getValue();
                        } catch(ControlFlow.Continue e) {
                            doAgain = true;
                        }
                    } while(doAgain);

                    return ret;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("loops forever - executing it's argument over and over until interrupted in some way.", new JavaMethod("loop") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRestUnevaluated("body")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    if(message.getArgumentCount() > 0) {
                        while(true) {
                            try {
                                while(true) {
                                    message.getEvaluatedArgument(0, context);
                                }
                            } catch(ControlFlow.Break e) {
                                return e.getValue();
                            } catch(ControlFlow.Continue e) {
                            }
                        }
                    } else {
                        while(true){}
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("evaluates the first arguments, and then evaluates the second argument if the result was true, otherwise the last argument. returns the result of the call, or the result if it's not true.", new JavaMethod("if") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("condition")
                    .withOptionalPositionalUnevaluated("then")
                    .withOptionalPositionalUnevaluated("else")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    Object test = message.getEvaluatedArgument(0, context);

                    LexicalContext itContext = new LexicalContext(context.runtime, context.getRealContext(), "Lexical activation context", message, context);
                    itContext.setCell("it", test);

                    if(IokeObject.isTrue(test)) {
                        if(message.getArgumentCount() > 1) {
                            return message.getEvaluatedArgument(1, itContext);
                        } else {
                            return test;
                        }
                    } else {
                        if(message.getArgumentCount() > 2) {
                            return message.getEvaluatedArgument(2, itContext);
                        } else {
                            return test;
                        }
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("evaluates the first arguments, and then evaluates the second argument if the result was false, otherwise the last argument. returns the result of the call, or the result if it's true.", new JavaMethod("unless") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("condition")
                    .withOptionalPositionalUnevaluated("then")
                    .withOptionalPositionalUnevaluated("else")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    Object test = message.getEvaluatedArgument(0, context);

                    LexicalContext itContext = new LexicalContext(context.runtime, context.getRealContext(), "Lexical activation context", message, context);
                    itContext.setCell("it", test);

                    if(IokeObject.isTrue(test)) {
                        if(message.getArgumentCount() > 2) {
                            return message.getEvaluatedArgument(2, itContext);
                        } else {
                            return test;
                        }
                    } else {
                        if(message.getArgumentCount() > 1) {
                            return message.getEvaluatedArgument(1, itContext);
                        } else {
                            return test;
                        }
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("takes zero or more arguments, calls asText on non-text arguments, and then concatenates them and returns the result.", new JavaMethod("internal:concatenateText") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRest("textSegments")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());

                    StringBuilder sb = new StringBuilder();

                    if(IokeObject.data(on) instanceof Text) {
                        sb.append(Text.getText(on));
                    }

                    for(Object o : args) {
                        if(IokeObject.data(o) instanceof Text) {
                            sb.append(Text.getText(o));
                        } else {
                            sb.append(Text.getText(context.runtime.asText.sendTo(context, o)));
                        }
                    }

                    return context.runtime.newText(sb.toString());
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects one 'strange' argument. creates a new instance of Text with the given Java String backing it.", new JavaMethod("internal:createText") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("text")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    Object o = Message.getArg1(message);
                    if(o instanceof String) {
                        String s = (String)o;
                        return runtime.newText(new StringUtils().replaceEscapes(s));
                    } else {
                        return IokeObject.convertToText(message.getEvaluatedArgument(0, context), message, context, true);
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects one 'strange' argument. creates a new mimic of Regexp with the given Java String backing it.", new JavaMethod("internal:createRegexp") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("regexp")
                    .withRequiredPositionalUnevaluated("flags")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    Object o = Message.getArg1(message);
                    if(o instanceof String) {
                        String s = (String)o;
                        return runtime.newRegexp(new StringUtils().replaceEscapes(s), context, message);
                    } else {
                        return IokeObject.convertToRegexp(message.getEvaluatedArgument(0, context), message, context);
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects one 'strange' argument. creates a new instance of Number that represents the number found in the strange argument.", new JavaMethod("internal:createNumber") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("number")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    String s = (String)Message.getArg1(message);
                    return runtime.newNumber(s);
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects one 'strange' argument. creates a new instance of Decimal that represents the number found in the strange argument.", new JavaMethod("internal:createDecimal") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("decimal")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    String s = (String)Message.getArg1(message);
                    return runtime.newDecimal(s);
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects one argument, which is the unevaluated name of the cell to work on. will retrieve the current value of this cell, call 'succ' to that value and then send = to the current receiver with the name and the resulting value.", new JavaMethod("++") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("place")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    IokeObject nameMessage = (IokeObject)Message.getArg1(message);
                    String name = nameMessage.getName();
                    Object current = IokeObject.as(on).perform(context, message, name);
                    Object value = runtime.succ.sendTo(context, current);
                    return runtime.setValue.sendTo(context, on, nameMessage, value);
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects one argument, which is the unevaluated name of the cell to work on. will retrieve the current value of this cell, call 'pred' to that value and then send = to the current receiver with the name and the resulting value.", new JavaMethod("--") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositionalUnevaluated("place")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    IokeObject nameMessage = (IokeObject)Message.getArg1(message);
                    String name = nameMessage.getName();
                    Object current = IokeObject.as(on).perform(context, message, name);
                    Object value = runtime.pred.sendTo(context, current);
                    return runtime.setValue.sendTo(context, on, nameMessage, value);
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("returns a textual representation of the object called on.", new JavaMethod.WithNoArguments("asText") {
                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().getEvaluatedArguments(context, message, on, new ArrayList<Object>(), new HashMap<String, Object>());

                    return runtime.newText(on.toString());
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects any number of unevaluated arguments. if no arguments at all are given, will just return nil. creates a new method based on the arguments. this method will be evaluated using the context of the object it's called on, and thus the definition can not refer to the outside scope where the method is defined. (there are other ways of achieving this). all arguments except the last one is expected to be names of arguments that will be used in the method. there will possible be additions to the format of arguments later on - including named parameters and optional arguments. the actual code is the last argument given.", new JavaMethod("method") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withOptionalPositionalUnevaluated("documentation")
                    .withRestUnevaluated("argumentsAndBody")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    List<Object> args = message.getArguments();

                    if(args.size() == 0) {
                        final Message mx = new Message(context.runtime, "nil", null, Message.Type.MESSAGE);
                        mx.setFile(Message.file(message));
                        mx.setLine(Message.line(message));
                        mx.setPosition(Message.position(message));
                        final IokeObject mmx = context.runtime.createMessage(mx);
                        return runtime.newMethod(null, runtime.defaultMethod, new DefaultMethod(context, DefaultArgumentsDefinition.empty(), mmx));
                    }

                    String doc = null;

                    List<String> argNames = new ArrayList<String>(args.size()-1);
                    int start = 0;
                    if(args.size() > 1 && ((IokeObject)Message.getArg1(message)).getName().equals("internal:createText")) {
                        start++;
                        String s = ((String)((IokeObject)args.get(0)).getArguments().get(0));
                        doc = s;
                    }

                    DefaultArgumentsDefinition def = DefaultArgumentsDefinition.createFrom(args, start, args.size()-1, message, on, context);

                    return runtime.newMethod(doc, runtime.defaultMethod, new DefaultMethod(context, def, (IokeObject)args.get(args.size()-1)));
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("expects one code argument, optionally preceeded by a documentation string. will create a new DefaultMacro based on the code and return it.", new JavaMethod("macro") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withOptionalPositionalUnevaluated("documentation")
                    .withOptionalPositionalUnevaluated("body")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    List<Object> args = message.getArguments();

                    if(args.size() == 0) {
                        final Message mx = new Message(context.runtime, "nil", null, Message.Type.MESSAGE);
                        mx.setFile(Message.file(message));
                        mx.setLine(Message.line(message));
                        mx.setPosition(Message.position(message));
                        final IokeObject mmx = context.runtime.createMessage(mx);

                        return runtime.newMacro(null, runtime.defaultMacro, new DefaultMacro(context, mmx));
                    }

                    String doc = null;

                    int start = 0;
                    if(args.size() > 1 && ((IokeObject)Message.getArg1(message)).getName().equals("internal:createText")) {
                        start++;
                        String s = ((String)((IokeObject)args.get(0)).getArguments().get(0));
                        doc = s;
                    }

                    return runtime.newMacro(doc, runtime.defaultMacro, new DefaultMacro(context, (IokeObject)args.get(start)));
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("creates a new lexical block that can be executed at will, while retaining a reference to the lexical closure it was created in. it will always update variables if they exist. there is currently no way of introducing shadowing variables in the local context. new variables can be created though, just like in a method. a lexical block mimics LexicalBlock, and can take arguments. at the moment these are restricted to required arguments, but support for the same argument types as DefaultMethod will come. same as fn()", new JavaMethod("ʎ") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withOptionalPositionalUnevaluated("documentation")
                    .withRestUnevaluated("argumentsAndBody")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    List<Object> args = message.getArguments();
                    if(args.isEmpty()) {
                        return runtime.newLexicalBlock(null, runtime.lexicalBlock, new LexicalBlock(context, DefaultArgumentsDefinition.empty(), method.runtime.nilMessage));
                    }

                    IokeObject code = IokeObject.as(args.get(args.size()-1));

                    DefaultArgumentsDefinition def = DefaultArgumentsDefinition.createFrom(args, 0, args.size()-1, message, on, context);
                    return runtime.newLexicalBlock(null, runtime.lexicalBlock, new LexicalBlock(context, def, code));
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("creates a new lexical block that can be executed at will, while retaining a reference to the lexical closure it was created in. it will always update variables if they exist. there is currently no way of introducing shadowing variables in the local context. new variables can be created though, just like in a method. a lexical block mimics LexicalBlock, and can take arguments. at the moment these are restricted to required arguments, but support for the same argument types as DefaultMethod will come.", new JavaMethod("fn") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withOptionalPositionalUnevaluated("documentation")
                    .withRestUnevaluated("argumentsAndBody")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    List<Object> args = message.getArguments();
                    if(args.isEmpty()) {
                        return runtime.newLexicalBlock(null, runtime.lexicalBlock, new LexicalBlock(context, DefaultArgumentsDefinition.empty(), method.runtime.nilMessage));
                    }

                    String doc = null;

                    List<String> argNames = new ArrayList<String>(args.size()-1);
                    int start = 0;
                    if(args.size() > 1 && ((IokeObject)Message.getArg1(message)).getName().equals("internal:createText")) {
                        start++;
                        String s = ((String)((IokeObject)args.get(0)).getArguments().get(0));
                        doc = s;
                    }

                    IokeObject code = IokeObject.as(args.get(args.size()-1));

                    DefaultArgumentsDefinition def = DefaultArgumentsDefinition.createFrom(args, start, args.size()-1, message, on, context);
                    return runtime.newLexicalBlock(doc, runtime.lexicalBlock, new LexicalBlock(context, def, code));
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("takes one or more evaluated string argument. will import the files corresponding to each of the strings named based on the Ioke loading behavior that can be found in the documentation for the loadBehavior cell on System.", new JavaMethod("use") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("module")
                    //                    .withRest("modules")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());
                    
                    String name = Text.getText(runtime.asText.sendTo(context, args.get(0)));
                    if(((IokeSystem)runtime.system.data).use(IokeObject.as(on), context, message, name)) {
                        return runtime._true;
                    } else {
                        return runtime._false;
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("takes one optional unevaluated parameter (this should be the first if provided), that is the name of the restart to create. this will default to nil. takes two keyword arguments, report: and test:. These should both be lexical blocks. if not provided, there will be reasonable defaults. the only required argument is something that evaluates into a lexical block. this block is what will be executed when the restart is invoked. will return a Restart mimic.", new JavaMethod("restart") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withOptionalPositionalUnevaluated("name")
                    .withKeyword("report")
                    .withKeyword("test")
                    .withRequiredPositional("action")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    String name = null;
                    IokeObject report = null;
                    IokeObject test = null;
                    IokeObject code = null;
                    final Runtime runtime = context.runtime;
                    
                    List<Object> args = message.getArguments();
                    int argCount = args.size();
                    if(argCount > 4) {
                        final IokeObject condition = IokeObject.as(IokeObject.getCellChain(runtime.condition, 
                                                                                     message, 
                                                                                     context, 
                                                                                     "Error", 
                                                                                     "Invocation", 
                                                                                     "TooManyArguments")).mimic(message, context);
                        condition.setCell("message", message);
                        condition.setCell("context", context);
                        condition.setCell("receiver", on);
                        condition.setCell("extra", runtime.newList(args.subList(4, argCount)));
                        runtime.withReturningRestart("ignoreExtraArguments", context, new RunnableWithControlFlow() {
                                public void run() throws ControlFlow {
                                    runtime.errorCondition(condition);
                                }});
                        argCount = 4;
                    } else if(argCount < 1) {
                        final IokeObject condition = IokeObject.as(IokeObject.getCellChain(runtime.condition, 
                                                                                           message, 
                                                                                           context, 
                                                                                           "Error", 
                                                                                           "Invocation", 
                                                                                           "TooFewArguments")).mimic(message, context);
                        condition.setCell("message", message);
                        condition.setCell("context", context);
                        condition.setCell("receiver", on);
                        condition.setCell("missing", runtime.newNumber(1-argCount));
                
                        runtime.errorCondition(condition);
                    }

                    for(int i=0; i<argCount; i++) {
                        Object o = args.get(i);
                        Message m = (Message)IokeObject.data(o);
                        if(m.isKeyword()) {
                            String n = m.getName(null);
                            if(n.equals("report:")) {
                                report = IokeObject.as(m.next.evaluateCompleteWithoutExplicitReceiver(context, context.getRealContext()));
                            } else if(n.equals("test:")) {
                                test = IokeObject.as(m.next.evaluateCompleteWithoutExplicitReceiver(context, context.getRealContext()));
                            } else {
                                final IokeObject condition = IokeObject.as(IokeObject.getCellChain(runtime.condition, 
                                                                                                   message, 
                                                                                                   context, 
                                                                                                   "Error", 
                                                                                                   "Invocation", 
                                                                                                   "MismatchedKeywords")).mimic(message, context);
                                condition.setCell("message", message);
                                condition.setCell("context", context);
                                condition.setCell("receiver", on);
                                condition.setCell("expected", runtime.newList(new ArrayList<Object>(Arrays.<Object>asList(runtime.newText("report:"), runtime.newText("test:")))));
                                List<Object> extra = new ArrayList<Object>();
                                extra.add(runtime.newText(n));
                                condition.setCell("extra", runtime.newList(extra));
                                
                                runtime.withReturningRestart("ignoreExtraKeywords", context, new RunnableWithControlFlow() {
                                        public void run() throws ControlFlow {
                                            runtime.errorCondition(condition);
                                        }});
                            }
                        } else {
                            if(code != null) {
                                name = code.getName();
                                code = IokeObject.as(o);
                            } else {
                                code = IokeObject.as(o);
                            }
                        }
                    }

                    code = IokeObject.as(code.evaluateCompleteWithoutExplicitReceiver(context, context.getRealContext()));
                    Object restart = runtime.mimic.sendTo(context, runtime.restart);
                    
                    IokeObject.setCell(restart, "code", code);

                    if(null != name) {
                        IokeObject.setCell(restart, "name", runtime.getSymbol(name));
                    }

                    if(null != test) {
                        IokeObject.setCell(restart, "test", test);
                    }

                    if(null != report) {
                        IokeObject.setCell(restart, "report", report);
                    }

                    return restart;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("takes zero or more arguments that should evaluate to a condition mimic - this list will match all the conditions this Rescue should be able to catch. the last argument is not optional, and should be something activatable that takes one argument - the condition instance. will return a Rescue mimic.", new JavaMethod("rescue") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRest("conditionsAndAction")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);
                    int count = message.getArgumentCount();
                    List<Object> conds = new ArrayList<Object>();
                    for(int i=0, j=count-1; i<j; i++) {
                        conds.add(message.getEvaluatedArgument(i, context));
                    }

                    if(conds.isEmpty()) {
                        conds.add(context.runtime.condition);
                    }

                    Object handler = message.getEvaluatedArgument(count-1, context);
                    Object rescue = context.runtime.mimic.sendTo(context, context.runtime.rescue);
                    
                    IokeObject.setCell(rescue, "handler", handler);
                    IokeObject.setCell(rescue, "conditions", context.runtime.newList(conds));

                    return rescue;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("takes zero or more arguments that should evaluate to a condition mimic - this list will match all the conditions this Handler should be able to catch. the last argument is not optional, and should be something activatable that takes one argument - the condition instance. will return a Handler mimic.", new JavaMethod("handle") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRest("conditionsAndAction")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    int count = message.getArgumentCount();
                    List<Object> conds = new ArrayList<Object>();
                    for(int i=0, j=count-1; i<j; i++) {
                        conds.add(message.getEvaluatedArgument(i, context));
                    }

                    if(conds.isEmpty()) {
                        conds.add(context.runtime.condition);
                    }

                    Object code = message.getEvaluatedArgument(count-1, context);
                    Object handle = context.runtime.mimic.sendTo(context, context.runtime.handler);
                    
                    IokeObject.setCell(handle, "handler", code);
                    IokeObject.setCell(handle, "conditions", context.runtime.newList(conds));

                    return handle;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("will evaluate all arguments, and expects all except for the last to be a Restart. bind will associate these restarts for the duration of the execution of the last argument and then unbind them again. it will return the result of the last argument, or if a restart is executed it will instead return the result of that invocation.", new JavaMethod("bind") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRestUnevaluated("bindablesAndCode")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, final IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    final Runtime runtime = context.runtime;
                    List<Object> args = message.getArguments();
                    int argCount = args.size();
                    if(argCount == 0) {
                        return context.runtime.nil;
                    }

                    IokeObject code = IokeObject.as(args.get(argCount-1));
                    List<Runtime.RestartInfo> restarts = new ArrayList<Runtime.RestartInfo>();
                    List<Runtime.RescueInfo> rescues = new ArrayList<Runtime.RescueInfo>();
                    List<Runtime.HandlerInfo> handlers = new ArrayList<Runtime.HandlerInfo>();

                    Runtime.BindIndex index = context.runtime.getBindIndex();

                    try {
                        for(Object o : args.subList(0, argCount-1)) {
                            IokeObject bindable = IokeObject.as(IokeObject.as(o).evaluateCompleteWithoutExplicitReceiver(context, context.getRealContext()));
                            boolean loop = false;
                            do {
                                loop = false;
                                if(IokeObject.isKind(bindable, "Restart")) {
                                    Object ioName = runtime.name.sendTo(context, bindable);
                                    String name = null;
                                    if(ioName != runtime.nil) {
                                        name = Symbol.getText(ioName);
                                    }
                            
                                    restarts.add(0, new Runtime.RestartInfo(name, bindable, restarts, index, null));
                                    index = index.nextCol();
                                } else if(IokeObject.isKind(bindable, "Rescue")) {
                                    Object conditions = runtime.conditionsMessage.sendTo(context, bindable);
                                    List<Object> applicable = IokeList.getList(conditions);
                                    rescues.add(0, new Runtime.RescueInfo(bindable, applicable, rescues, index));
                                    index = index.nextCol();
                                } else if(IokeObject.isKind(bindable, "Handler")) {
                                    Object conditions = runtime.conditionsMessage.sendTo(context, bindable);
                                    List<Object> applicable = IokeList.getList(conditions);
                                    handlers.add(0, new Runtime.HandlerInfo(bindable, applicable, handlers, index));
                                    index = index.nextCol();
                                } else {
                                    final IokeObject condition = IokeObject.as(IokeObject.getCellChain(runtime.condition, 
                                                                                                       message, 
                                                                                                       context, 
                                                                                                       "Error", 
                                                                                                       "Type",
                                                                                                       "IncorrectType")).mimic(message, context);
                                    condition.setCell("message", message);
                                    condition.setCell("context", context);
                                    condition.setCell("receiver", on);
                                    condition.setCell("expectedType", runtime.getSymbol("Bindable"));
                        
                                    final Object[] newCell = new Object[]{bindable};
                        
                                    runtime.withRestartReturningArguments(new RunnableWithControlFlow() {
                                            public void run() throws ControlFlow {
                                                runtime.errorCondition(condition);
                                            }}, 
                                        context,
                                        new Restart.ArgumentGivingRestart("useValue") { 
                                            public List<String> getArgumentNames() {
                                                return new ArrayList<String>(Arrays.asList("newValue"));
                                            }
                                    
                                            public IokeObject invoke(IokeObject context, List<Object> arguments) throws ControlFlow {
                                                newCell[0] = arguments.get(0);
                                                return runtime.nil;
                                            }
                                        }
                                        );
                                    bindable = IokeObject.as(newCell[0]);
                                    loop = true;
                                }
                            } while(loop);
                            loop = false;
                        }
                        runtime.registerRestarts(restarts);
                        runtime.registerRescues(rescues);
                        runtime.registerHandlers(handlers);

                        return code.evaluateCompleteWithoutExplicitReceiver(context, context.getRealContext());
                    } catch(ControlFlow.Restart e) {
                        Runtime.RestartInfo ri = null;
                        if((ri = e.getRestart()).token == restarts) {
                            // Might need to unregister restarts before doing this...
                            return runtime.callMessage.sendTo(context, runtime.code.sendTo(context, ri.restart), e.getArguments());
                        } else {
                            throw e;
                        } 
                    } catch(ControlFlow.Rescue e) {
                        Runtime.RescueInfo ri = null;
                        if((ri = e.getRescue()).token == rescues) {
                            return runtime.callMessage.sendTo(context, runtime.handlerMessage.sendTo(context, ri.rescue), e.getCondition());
                        } else {
                            throw e;
                        }
                   } finally {
                        runtime.unregisterHandlers(handlers);
                        runtime.unregisterRescues(rescues);
                        runtime.unregisterRestarts(restarts); 
                   }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("takes either a name (as a symbol) or a Restart instance. if the restart is active, will transfer control to it, supplying the rest of the given arguments to that restart.", new JavaMethod("invokeRestart") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("nameOrRestart")
                    .withRest("arguments")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> posArgs = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, posArgs, new HashMap<String, Object>());

                    final Runtime runtime = context.runtime;

                    IokeObject restart = IokeObject.as(posArgs.get(0));
                    Runtime.RestartInfo realRestart = null;
                    List<Object> args = new ArrayList<Object>();
                    if(restart.isSymbol()) {
                        String name = Symbol.getText(restart);
                        realRestart = context.runtime.findActiveRestart(name);
                        if(null == realRestart) {
                            final IokeObject condition = IokeObject.as(IokeObject.getCellChain(runtime.condition, 
                                                                                               message, 
                                                                                               context, 
                                                                                               "Error", 
                                                                                               "RestartNotActive")).mimic(message, context);
                            condition.setCell("message", message);
                            condition.setCell("context", context);
                            condition.setCell("receiver", on);
                            condition.setCell("restart", restart);
                            
                            runtime.withReturningRestart("ignoreMissingRestart", context, new RunnableWithControlFlow() {
                                    public void run() throws ControlFlow {
                                        runtime.errorCondition(condition);
                                    }});
                            return runtime.nil;
                        }
                    } else {
                        realRestart = context.runtime.findActiveRestart(restart);
                        if(null == realRestart) {
                            final IokeObject condition = IokeObject.as(IokeObject.getCellChain(runtime.condition, 
                                                                                               message, 
                                                                                               context, 
                                                                                               "Error", 
                                                                                               "RestartNotActive")).mimic(message, context);
                            condition.setCell("message", message);
                            condition.setCell("context", context);
                            condition.setCell("receiver", on);
                            condition.setCell("restart", restart);
                            
                            runtime.withReturningRestart("ignoreMissingRestart", context, new RunnableWithControlFlow() {
                                    public void run() throws ControlFlow {
                                        runtime.errorCondition(condition);
                                    }});
                            return runtime.nil;
                        }
                    }

                    int argCount = posArgs.size();
                    for(int i = 1;i<argCount;i++) {
                        args.add(posArgs.get(i));
                    }

                    throw new ControlFlow.Restart(realRestart, args);
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("takes either a name (as a symbol) or a Restart instance. if the restart is active, will return that restart, otherwise returns nil.", new JavaMethod("findRestart") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("nameOrRestart")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, final IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());

                    final Runtime runtime = context.runtime;
                    IokeObject restart = IokeObject.as(args.get(0));
                    Runtime.RestartInfo realRestart = null;
                    while(!(restart.isSymbol() || restart.getKind().equals("Restart"))) {
                        final IokeObject condition = IokeObject.as(IokeObject.getCellChain(runtime.condition, 
                                                                                           message, 
                                                                                           context, 
                                                                                           "Error", 
                                                                                           "Type",
                                                                                           "IncorrectType")).mimic(message, context);
                        condition.setCell("message", message);
                        condition.setCell("context", context);
                        condition.setCell("receiver", on);
                        condition.setCell("expectedType", runtime.getSymbol("Restart"));
                        
                        final Object[] newCell = new Object[]{restart};
                        
                        runtime.withRestartReturningArguments(new RunnableWithControlFlow() {
                                public void run() throws ControlFlow {
                                    runtime.errorCondition(condition);
                                }}, 
                            context,
                            new Restart.ArgumentGivingRestart("useValue") { 
                                public List<String> getArgumentNames() {
                                    return new ArrayList<String>(Arrays.asList("newValue"));
                                }
                                    
                                public IokeObject invoke(IokeObject context, List<Object> arguments) throws ControlFlow {
                                    newCell[0] = arguments.get(0);
                                    return runtime.nil;
                                }
                            }
                            );
                        restart = IokeObject.as(newCell[0]);
                    }

                    if(restart.isSymbol()) {
                        String name = Symbol.getText(restart);
                        realRestart = runtime.findActiveRestart(name);
                    } else if(restart.getKind().equals("Restart")) {
                        realRestart = runtime.findActiveRestart(restart);
                    }
                    if(realRestart == null) {
                        return runtime.nil;
                    } else {
                        return realRestart.restart;
                    }
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("takes an optional condition to specify - returns all restarts that are applicable to that condition. closer restarts will be first in the list", new JavaMethod("availableRestarts") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withOptionalPositional("condition", "Condition")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, final IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());
                    final Runtime runtime = context.runtime;

                    Object toLookFor = runtime.condition;
                    if(args.size() > 0) {
                        toLookFor = args.get(0);
                    }

                    List<Object> result = new ArrayList<Object>();
                    List<List<Runtime.RestartInfo>> activeRestarts = runtime.getActiveRestarts();

                    for(List<Runtime.RestartInfo> lri : activeRestarts) {
                        for(Runtime.RestartInfo rri : lri) {
                            if(IokeObject.isTrue(runtime.callMessage.sendTo(context, runtime.testMessage.sendTo(context, rri.restart), toLookFor))) {
                                result.add(rri.restart);
                            }
                        }
                    }

                    return runtime.newList(result);
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("takes one or more datums descibing the condition to signal. this datum can be either a mimic of a Condition, in which case it will be signalled directly, or it can be a mimic of a Condition with arguments, in which case it will first be mimicked and the arguments assigned in some way. finally, if the argument is a Text, a mimic of Condition Default will be signalled, with the provided text.", new JavaMethod("signal!") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("datum")
                    .withKeywordRest("conditionArguments")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> positionalArgs = new ArrayList<Object>();
                    Map<String, Object> keywordArgs = new HashMap<String, Object>();
                    getArguments().getEvaluatedArguments(context, message, on, positionalArgs, keywordArgs);

                    Object datum = positionalArgs.get(0);
                    
                    return signal(datum, positionalArgs, keywordArgs, message, context);
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("takes the same kind of arguments as 'signal!', and will signal a condition. the default condition used is Condition Error Default. if no rescue or restart is invoked error! will report the condition to System err and exit the currently running Ioke VM. this might be a problem when exceptions happen inside of running Java code, as callbacks and so on.. if 'System currentDebugger' is non-nil, it will be invoked before the exiting of the VM. the exit can only be avoided by invoking a restart. that means that error! will never return. ", new JavaMethod("error!") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("datum")
                    .withKeywordRest("errorArguments")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> positionalArgs = new ArrayList<Object>();
                    Map<String, Object> keywordArgs = new HashMap<String, Object>();
                    getArguments().getEvaluatedArguments(context, message, on, positionalArgs, keywordArgs);

                    Object datum = positionalArgs.get(0);

                    if(IokeObject.data(datum) instanceof Text) {
                        Object oldDatum = datum;
                        datum = IokeObject.as(IokeObject.as(context.runtime.condition.getCell(message, context, "Error")).getCell(message, context, "Default")).mimic(message, context);
                        IokeObject.setCell(datum, message, context, "text", oldDatum);
                    }

                    IokeObject condition = signal(datum, positionalArgs, keywordArgs, message, context);
                    IokeObject err = IokeObject.as(context.runtime.system.getCell(message, context, "err"));
                    
                    context.runtime.printMessage.sendTo(context, err, context.runtime.newText("*** - "));
                    context.runtime.printlnMessage.sendTo(context, err, context.runtime.reportMessage.sendTo(context, condition));
                    
                    IokeObject currentDebugger = IokeObject.as(context.runtime.currentDebuggerMessage.sendTo(context, context.runtime.system));

                    if(!currentDebugger.isNil()) {
                        context.runtime.invokeMessage.sendTo(context, currentDebugger, condition, context);
                    }

                    throw new ControlFlow.Exit(condition);
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("Takes one evaluated Text argument and returns either true or false if this object or one of it's mimics have the kind of the name specified", new JavaMethod("kind?") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("name")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());

                    String kind = Text.getText(args.get(0));
                    return IokeObject.isKind(on, kind) ? context.runtime._true : context.runtime._false;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("Takes one evaluated argument and returns either true or false if this object or one of it's mimics mimics that argument", new JavaMethod("mimics?") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("potentialMimic")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());

                    IokeObject arg = IokeObject.as(args.get(0));
                    return IokeObject.isMimic(on, arg) ? context.runtime._true : context.runtime._false;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("Takes one evaluated argument and returns either true or false if this object or one of it's mimics mimics that argument. exactly the same as 'mimics?'", new JavaMethod("is?") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("potentialMimic")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());

                    IokeObject arg = IokeObject.as(args.get(0));
                    return IokeObject.isMimic(on, arg) ? context.runtime._true : context.runtime._false;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("returns a list of all the mimics of the receiver. it will not be the same list as is used to back the object, so modifications to this list will not show up in the object.", new JavaMethod.WithNoArguments("mimics") {
                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().getEvaluatedArguments(context, message, on, new ArrayList<Object>(), new HashMap<String, Object>());

                    return context.runtime.newList(new ArrayList<Object>(IokeObject.getMimics(on)));
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("removes all mimics on the receiver, and returns the receiver", new JavaMethod.WithNoArguments("removeAllMimics!") {
                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().getEvaluatedArguments(context, message, on, new ArrayList<Object>(), new HashMap<String, Object>());

                    IokeObject.getMimics(on).clear();
                    return on;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("removes the argument mimic from the list of all mimics on the receiver. will do nothing if the receiver has no such mimic. it returns the receiver", new JavaMethod("removeMimic!") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("mimicToRemove")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());

                    IokeObject.getMimics(on).remove(args.get(0));
                    return on;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("Takes one evaluated argument and adds it to the list of mimics for the receiver. the receiver will be returned.", new JavaMethod("mimic!") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("newMimic")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());

                    IokeObject newMimic = IokeObject.as(args.get(0));
                    IokeObject.as(on).mimics(newMimic, message, context);
                    return on;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("Takes one evaluated argument and prepends it to the list of mimics for the receiver. the receiver will be returned.", new JavaMethod("prependMimic!") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("newMimic")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());

                    IokeObject newMimic = IokeObject.as(args.get(0));
                    IokeObject.as(on).mimics(0, newMimic, message, context);
                    return on;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("Takes two evaluated text or symbol arguments that name the method to alias, and the new name to give it. returns the receiver.", new JavaMethod("aliasMethod") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("oldName")
                    .withRequiredPositional("newName")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());

                    String fromName = Text.getText(runtime.asText.sendTo(context, args.get(0)));
                    String toName = Text.getText(runtime.asText.sendTo(context, args.get(1)));
                    IokeObject.as(on).aliasMethod(fromName, toName);
                    return on;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("Takes one evaluated argument and returns a new Pair of the receiver and the argument", new JavaMethod("=>") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("other")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());

                    return context.runtime.newPair(on, args.get(0));
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("Takes one evaluated argument that is expected to be a Text, and returns the symbol corresponding to that text", new JavaMethod(":") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRequiredPositional("symbolText")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());

                    String sym = Text.getText(runtime.asText.sendTo(context, args.get(0)));
                    return context.runtime.getSymbol(sym);
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("creates a new Dict from the arguments provided. these arguments can be two different things - either a keyword argument, or a pair. if it's a keyword argument, the entry added to the dict for it will be a symbol with the name from the keyword, without the ending colon. if it's not a keyword, it is expected to be an evaluated pair, where the first part of the pair is the key, and the second part is the value.", new JavaMethod("dict") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRest("pairs")
                    .withKeywordRest("keywordPairs")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    List<Object> arguments = message.getArguments();
                    Map<Object, Object> moo = new HashMap<Object, Object>(arguments.size());

                    for(Object o : arguments) {
                        Object key, value;
                        if(Message.isKeyword(o)) {
                            String str = Message.name(o);
                            key = context.runtime.getSymbol(str.substring(0, str.length()-1));
                            if(Message.next(o) != null) {
                                value = Message.getEvaluatedArgument(Message.next(o), context);
                            } else {
                                value = context.runtime.nil;
                            }
                        } else {
                            Object result = Message.getEvaluatedArgument(o, context);
                            if((result instanceof IokeObject) && (IokeObject.data(result) instanceof Pair)) {
                                key = Pair.getFirst(result);
                                value = Pair.getSecond(result);
                            } else {
                                key = result;
                                value = context.runtime.nil;
                            }
                        }

                        moo.put(key, value);
                    }

                    return context.runtime.newDict(moo);
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("creates a new Set from the result of evaluating all arguments provided.", new JavaMethod("set") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withRest("elements")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    List<Object> args = new ArrayList<Object>();
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());

                    return context.runtime.newSet(args);
                }
            }));
    }
}// DefaultBehavior
