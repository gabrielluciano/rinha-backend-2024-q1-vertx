FROM debian:bookworm

WORKDIR /work/

RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work

COPY --chown=1001:root rinha-1.0.0 /work/rinha

EXPOSE 8080 8081
USER 1001

CMD ["./rinha"]
