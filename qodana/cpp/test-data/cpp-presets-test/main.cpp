int main()
{
#ifdef BUGS
	const int *x = nullptr;
	return *x;
#else
	return 0;
#endif
}
