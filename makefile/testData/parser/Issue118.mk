$(OBJDIR)/%.o: %.cpp ${PCH}
	$(GCC) -MMD $(CFLAGS) $(PCHINC) -c $< -o $@

a: b $(eval)
a: c
	touch $@