GSON type adapter factory implementation for polymorphic class hierarchies which may include generics. 

For usage, check the tests in src/test/java/ai/shape/basics/gson

This module depends on a modified version of google's gson library. The modification is 
described and proposed in https://github.com/google/gson/pull/1455  If we upgrade the 
gson library and Google didn't include the pull request, then we need to reapply this change. 